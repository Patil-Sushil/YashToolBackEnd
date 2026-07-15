package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceItemResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoiceItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.mapper.PurchaseInvoiceMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {

    private final PurchaseInvoiceRepository repository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    private final PurchaseInvoiceMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Purchase Invoice", action = AuditAction.PURCHASE_INVOICE_CREATED, entityType = "PURCHASE_INVOICE")
    public PurchaseInvoiceResponse create(PurchaseInvoiceRequest request) {
        log.info("Recording Purchase Invoice. Supplier invoice number: {}", request.getSupplierInvoiceNumber());

        repository.findBySupplierInvoiceNumber(request.getSupplierInvoiceNumber().trim()).ifPresent(i -> {
            throw new BusinessException("Supplier invoice number '" + request.getSupplierInvoiceNumber() + "' already exists.");
        });

        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + request.getPurchaseOrderId()));

        List<GoodsReceipt> grns = new ArrayList<>();
        for (UUID grnId : request.getGoodsReceiptIds()) {
            GoodsReceipt grn = goodsReceiptRepository.findById(grnId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt Note not found with ID: " + grnId));

            if (!grn.getPurchaseOrder().getId().equals(po.getId())) {
                throw new BusinessException("GRN " + grn.getGrnNumber() + " is not associated with Purchase Order " + po.getPoNumber());
            }
            grns.add(grn);
        }

        PurchaseInvoice invoice = mapper.toEntity(request);
        invoice.setPurchaseOrder(po);
        invoice.setVendor(po.getVendor());
        invoice.setGoodsReceipts(grns);
        if (po.getCompany() != null) {
            invoice.setCompany(po.getCompany());
        }

        LocalDate invoiceDate = request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now();
        invoice.setInvoiceDate(invoiceDate);

        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        } else {
            LocalDate dueDate = invoiceDate;
            if (po.getVendor() != null && po.getVendor().getPaymentTerms() != null) {
                dueDate = invoiceDate.plusDays(po.getVendor().getPaymentTerms().getNumberOfDays());
            }
            invoice.setDueDate(dueDate);
        }

        invoice.setInvoiceNumber(generateInvoiceNumber(invoice.getCompany() != null ? invoice.getCompany().getCode() : "YT"));
        invoice.setItems(new ArrayList<>());

        if (request.getFreight() != null) {
            invoice.setFreight(request.getFreight());
        }
        if (request.getOtherCharges() != null) {
            invoice.setOtherCharges(request.getOtherCharges());
        }

        BigDecimal invoiceTotal = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemReq.getItemId()));

            MaterialGrade materialGrade = null;
            if (itemReq.getMaterialGradeId() != null) {
                materialGrade = materialGradeRepository.findById(itemReq.getMaterialGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + itemReq.getMaterialGradeId()));
            }

            BigDecimal taxableAmount = itemReq.getInvoiceQuantity().multiply(itemReq.getInvoiceRate());
            BigDecimal gstAmount = itemReq.getGst();

            if (gstAmount == null) {
                // Find matching PO item to get the GST percentage
                final MaterialGrade finalGrade = materialGrade;
                Optional<PurchaseOrderItem> poItemOpt = po.getItems().stream()
                        .filter(poi -> poi.getItem().getId().equals(item.getId()) &&
                                ((poi.getMaterialGrade() == null && finalGrade == null) ||
                                 (poi.getMaterialGrade() != null && finalGrade != null && poi.getMaterialGrade().getId().equals(finalGrade.getId()))))
                        .findFirst();

                BigDecimal gstPercentage = poItemOpt.map(PurchaseOrderItem::getGstPercentage).orElse(BigDecimal.ZERO);
                gstAmount = taxableAmount.multiply(gstPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }

            BigDecimal totalAmount = taxableAmount.add(gstAmount);
            invoiceTotal = invoiceTotal.add(totalAmount);

            PurchaseInvoiceItem invoiceItem = mapper.toItemEntity(itemReq);
            invoiceItem.setItem(item);
            invoiceItem.setMaterialGrade(materialGrade);
            invoiceItem.setTaxableAmount(taxableAmount);
            invoiceItem.setGst(gstAmount);
            invoiceItem.setTotalAmount(totalAmount);

            invoice.addItem(invoiceItem);
        }

        invoiceTotal = invoiceTotal.add(invoice.getFreight()).add(invoice.getOtherCharges());
        invoice.setTotalAmount(invoiceTotal);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(invoiceTotal);
        invoice.setPaymentStatus(com.kalibyte.YashTools.purchase.shared.enums.PaymentStatus.PENDING);
        invoice.setStatus(com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.APPROVED);

        PurchaseInvoice saved = repository.save(invoice);
        return mapToResponseWithWarnings(saved);
    }

    @Override
    public PurchaseInvoiceResponse getById(UUID id) {
        PurchaseInvoice invoice = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found with ID: " + id));
        return mapToResponseWithWarnings(invoice);
    }

    @Override
    public List<PurchaseInvoiceResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponseWithWarnings)
                .toList();
    }

    private PurchaseInvoiceResponse mapToResponseWithWarnings(PurchaseInvoice invoice) {
        PurchaseInvoiceResponse response = mapper.toResponse(invoice);

        // Populate GRN links
        List<UUID> grnIds = invoice.getGoodsReceipts().stream().map(GoodsReceipt::getId).toList();
        List<String> grnNumbers = invoice.getGoodsReceipts().stream().map(GoodsReceipt::getGrnNumber).toList();
        response.setGoodsReceiptIds(grnIds);
        response.setGoodsReceiptNumbers(grnNumbers);

        // Generate quantity discrepancy warnings
        List<String> warnings = generateWarnings(invoice);
        response.setWarnings(warnings);

        return response;
    }

    private List<String> generateWarnings(PurchaseInvoice invoice) {
        List<String> warnings = new ArrayList<>();
        PurchaseOrder po = invoice.getPurchaseOrder();
        List<GoodsReceipt> grns = invoice.getGoodsReceipts();

        for (PurchaseInvoiceItem invItem : invoice.getItems()) {
            Item item = invItem.getItem();
            MaterialGrade grade = invItem.getMaterialGrade();

            // 1. Resolve PO Quantity
            BigDecimal poQty = po.getItems().stream()
                    .filter(poi -> poi.getItem().getId().equals(item.getId()) &&
                            ((poi.getMaterialGrade() == null && grade == null) ||
                             (poi.getMaterialGrade() != null && grade != null && poi.getMaterialGrade().getId().equals(grade.getId()))))
                    .map(PurchaseOrderItem::getOrderedQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2. Resolve GRN Quantity (across mapped GRNs)
            BigDecimal grnQty = BigDecimal.ZERO;
            for (GoodsReceipt grn : grns) {
                BigDecimal grnItemQty = grn.getItems().stream()
                        .filter(gri -> gri.getItem().getId().equals(item.getId()) &&
                                ((gri.getMaterialGrade() == null && grade == null) ||
                                 (gri.getMaterialGrade() != null && grade != null && gri.getMaterialGrade().getId().equals(grade.getId()))))
                        .map(GoodsReceiptItem::getAcceptedQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                grnQty = grnQty.add(grnItemQty);
            }

            BigDecimal invQty = invItem.getInvoiceQuantity();
            String itemIdentifier = item.getName() + " (SKU: " + item.getSku() + ")";

            // Compare quantities and issue warnings
            if (poQty.compareTo(BigDecimal.ZERO) > 0 && poQty.compareTo(grnQty) != 0) {
                warnings.add(String.format("Item '%s': Ordered quantity in PO (%s) differs from accepted quantity in GRNs (%s)",
                        itemIdentifier, poQty.stripTrailingZeros().toPlainString(), grnQty.stripTrailingZeros().toPlainString()));
            }
            if (grnQty.compareTo(invQty) != 0) {
                warnings.add(String.format("Item '%s': Accepted quantity in GRNs (%s) differs from invoiced quantity (%s)",
                        itemIdentifier, grnQty.stripTrailingZeros().toPlainString(), invQty.stripTrailingZeros().toPlainString()));
            }
            if (poQty.compareTo(BigDecimal.ZERO) > 0 && poQty.compareTo(invQty) != 0) {
                warnings.add(String.format("Item '%s': Ordered quantity in PO (%s) differs from invoiced quantity (%s)",
                        itemIdentifier, poQty.stripTrailingZeros().toPlainString(), invQty.stripTrailingZeros().toPlainString()));
            }
        }

        return warnings;
    }

    private String generateInvoiceNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<PurchaseInvoice> lastInvoice = repository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastInvoice.isPresent()) {
            String lastNumber = lastInvoice.get().getInvoiceNumber();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 4) { // E.g., YT-PINV-2026-00001
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1
                }
            }
        }

        String companyPrefix = companyCode != null && !companyCode.trim().isEmpty() ? companyCode.trim() : "YT";
        return String.format("%s-PINV-%d-%05d", companyPrefix, year, nextNumber);
    }

    @Override
    @Transactional
    @LoggableAction(value = "Update Purchase Invoice Status", action = AuditAction.PURCHASE_INVOICE_UPDATED, entityType = "PURCHASE_INVOICE")
    public PurchaseInvoiceResponse updateStatus(UUID id, com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.UpdatePurchaseInvoiceStatusRequest request) {
        log.info("Updating status of purchase invoice: {} to {}", id, request.getStatus());
        PurchaseInvoice invoice = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found with ID: " + id));

        if (request.getStatus() == com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.CANCELLED) {
            if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("Cannot cancel Purchase Invoice because payments have already been made against it.");
            }
            invoice.setOutstandingAmount(BigDecimal.ZERO);
        } else if (request.getStatus() == com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus.APPROVED) {
            invoice.setOutstandingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        }

        invoice.setStatus(request.getStatus());
        PurchaseInvoice saved = repository.save(invoice);
        return mapToResponseWithWarnings(saved);
    }
}
