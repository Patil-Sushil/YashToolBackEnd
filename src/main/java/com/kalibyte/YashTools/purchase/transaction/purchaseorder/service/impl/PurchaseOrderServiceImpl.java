package com.kalibyte.YashTools.purchase.transaction.purchaseorder.service.impl;

import java.util.Optional;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import com.kalibyte.YashTools.purchase.master.paymentterms.repository.PaymentTermsRepository;
import com.kalibyte.YashTools.purchase.master.purchasetype.entity.PurchaseType;
import com.kalibyte.YashTools.purchase.master.purchasetype.repository.PurchaseTypeRepository;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.master.vendor.repository.VendorRepository;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.UpdatePurchaseOrderStatusRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.mapper.PurchaseOrderMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.service.PurchaseOrderService;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.validator.PurchaseOrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final VendorRepository vendorRepository;
    private final PaymentTermsRepository paymentTermsRepository;
    private final PurchaseTypeRepository purchaseTypeRepository;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    private final PurchaseOrderMapper mapper;
    private final PurchaseOrderValidator validator;

    @Override
    @Transactional
    @LoggableAction(value = "Create Purchase Order", action = AuditAction.PURCHASE_ORDER_CREATED, entityType = "PURCHASE_ORDER")
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        log.info("Creating purchase order for vendor: {}", request.getVendorId());
        
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));

        PurchaseOrder po = mapper.toEntity(request);
        po.setVendor(vendor);
        
        if (request.getPoDate() == null) {
            po.setPoDate(LocalDate.now());
        }

        if (request.getPaymentTermsId() != null) {
            PaymentTerms pt = paymentTermsRepository.findById(request.getPaymentTermsId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment Terms not found with ID: " + request.getPaymentTermsId()));
            po.setPaymentTerms(pt);
        } else if (vendor.getPaymentTerms() != null) {
            po.setPaymentTerms(vendor.getPaymentTerms());
        }

        if (request.getPurchaseTypeId() != null) {
            PurchaseType pt = purchaseTypeRepository.findById(request.getPurchaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Type not found with ID: " + request.getPurchaseTypeId()));
            po.setPurchaseType(pt);
        }

        // Set company context from vendor (or listener will handle it, but setting it explicitly is safer)
        if (vendor.getCompany() != null) {
            po.setCompany(vendor.getCompany());
        }

        po.setPoNumber(generatePoNumber(po.getCompany() != null ? po.getCompany().getCode() : "YT"));
        po.setStatus(PurchaseOrderStatus.DRAFT);

        // Map and link items
        po.setItems(new ArrayList<>());
        for (var itemReq : request.getItems()) {
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemReq.getItemId()));
            
            MaterialGrade materialGrade = null;
            if (itemReq.getMaterialGradeId() != null) {
                materialGrade = materialGradeRepository.findById(itemReq.getMaterialGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + itemReq.getMaterialGradeId()));
            }

            BigDecimal gst = itemReq.getGstPercentage() != null ? itemReq.getGstPercentage() : BigDecimal.ZERO;
            BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;

            BigDecimal taxable = itemReq.getOrderedQuantity().multiply(itemReq.getRate()).subtract(discount);
            BigDecimal gstAmount = taxable.multiply(gst).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal lineTotal = taxable.add(gstAmount);

            PurchaseOrderItem itemEntity = mapper.toItemEntity(itemReq);
            itemEntity.setItem(item);
            itemEntity.setMaterialGrade(materialGrade);
            itemEntity.setGstPercentage(gst);
            itemEntity.setDiscount(discount);
            itemEntity.setLineTotal(lineTotal);

            po.addItem(itemEntity);
        }

        validator.validateForSave(po);

        PurchaseOrder saved = repository.save(po);
        return mapper.toResponse(saved);
    }

    @Override
    public PurchaseOrderResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
    }

    @Override
    public List<PurchaseOrderResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Purchase Order", action = AuditAction.PURCHASE_ORDER_UPDATED, entityType = "PURCHASE_ORDER")
    public PurchaseOrderResponse update(UUID id, PurchaseOrderRequest request) {
        log.info("Updating purchase order: {}", id);
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED || po.getStatus() == PurchaseOrderStatus.CLOSED) {
            throw new BusinessException("Cannot edit Purchase Order in " + po.getStatus() + " status.");
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));

        mapper.updateEntityFromRequest(request, po);
        po.setVendor(vendor);

        if (request.getPaymentTermsId() != null) {
            PaymentTerms pt = paymentTermsRepository.findById(request.getPaymentTermsId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment Terms not found with ID: " + request.getPaymentTermsId()));
            po.setPaymentTerms(pt);
        } else {
            po.setPaymentTerms(null);
        }

        if (request.getPurchaseTypeId() != null) {
            PurchaseType pt = purchaseTypeRepository.findById(request.getPurchaseTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Type not found with ID: " + request.getPurchaseTypeId()));
            po.setPurchaseType(pt);
        } else {
            po.setPurchaseType(null);
        }

        // Recreate items to handle additions/updates/deletions easily
        po.getItems().clear();
        for (var itemReq : request.getItems()) {
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemReq.getItemId()));
            
            MaterialGrade materialGrade = null;
            if (itemReq.getMaterialGradeId() != null) {
                materialGrade = materialGradeRepository.findById(itemReq.getMaterialGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + itemReq.getMaterialGradeId()));
            }

            BigDecimal gst = itemReq.getGstPercentage() != null ? itemReq.getGstPercentage() : BigDecimal.ZERO;
            BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;

            BigDecimal taxable = itemReq.getOrderedQuantity().multiply(itemReq.getRate()).subtract(discount);
            BigDecimal gstAmount = taxable.multiply(gst).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal lineTotal = taxable.add(gstAmount);

            PurchaseOrderItem itemEntity = mapper.toItemEntity(itemReq);
            itemEntity.setItem(item);
            itemEntity.setMaterialGrade(materialGrade);
            itemEntity.setGstPercentage(gst);
            itemEntity.setDiscount(discount);
            itemEntity.setLineTotal(lineTotal);

            po.addItem(itemEntity);
        }

        validator.validateForSave(po);

        PurchaseOrder saved = repository.save(po);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Purchase Order Status", action = AuditAction.PURCHASE_ORDER_STATUS_CHANGED, entityType = "PURCHASE_ORDER")
    public PurchaseOrderResponse updateStatus(UUID id, UpdatePurchaseOrderStatusRequest request) {
        log.info("Updating status of purchase order: {} to {}", id, request.getStatus());
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        validator.validateStatusTransition(po.getStatus(), request.getStatus());

        po.setStatus(request.getStatus());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            po.setRemarks(po.getRemarks() == null ? request.getRemarks() : po.getRemarks() + " | " + request.getRemarks());
        }

        PurchaseOrder saved = repository.save(po);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Delete Purchase Order", action = AuditAction.PURCHASE_ORDER_DELETED, entityType = "PURCHASE_ORDER")
    public void delete(UUID id) {
        log.info("Deleting purchase order: {}", id);
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Only Draft Purchase Orders can be deleted. Current status: " + po.getStatus());
        }

        repository.delete(po);
    }

    private String generatePoNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<PurchaseOrder> lastPo = repository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastPo.isPresent()) {
            String lastNumber = lastPo.get().getPoNumber();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 4) { // E.g., YT-PO-2026-00001
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1
                }
            }
        }

        String companyPrefix = companyCode != null && !companyCode.trim().isEmpty() ? companyCode.trim() : "YT";
        return String.format("%s-PO-%d-%05d", companyPrefix, year, nextNumber);
    }
}
