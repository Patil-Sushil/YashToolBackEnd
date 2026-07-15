package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.mapper.GoodsReceiptMapper;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptItemRepository;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptRepository;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.service.GoodsReceiptService;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private final GoodsReceiptRepository repository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockService stockService;
    private final StockTransactionService stockTransactionService;
    private final GoodsReceiptMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Goods Receipt Note", action = AuditAction.GRN_CREATED, entityType = "GOODS_RECEIPT")
    public GoodsReceiptResponse create(GoodsReceiptRequest request) {
        log.info("Creating Goods Receipt Note (GRN) for Purchase Order: {}", request.getPurchaseOrderId());

        PurchaseOrder po = purchaseOrderRepository.findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + request.getPurchaseOrderId()));

        if (po.getStatus() != PurchaseOrderStatus.APPROVED && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new BusinessException("Only Approved or Partially Received Purchase Orders can be inwarded. Current status: " + po.getStatus());
        }

        GoodsReceipt grn = mapper.toEntity(request);
        grn.setPurchaseOrder(po);
        grn.setVendor(po.getVendor());
        if (po.getCompany() != null) {
            grn.setCompany(po.getCompany());
        }

        if (request.getGrnDate() == null) {
            grn.setGrnDate(LocalDate.now());
        }

        grn.setGrnNumber(generateGrnNumber(grn.getCompany() != null ? grn.getCompany().getCode() : "YT"));
        grn.setItems(new ArrayList<>());

        boolean allPoItemsFullyReceived = true;
        boolean anyReceived = false;

        for (var itemReq : request.getItems()) {
            PurchaseOrderItem poItem = po.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.getPoItemReferenceId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("PO Item Reference not found with ID: " + itemReq.getPoItemReferenceId()));

            BigDecimal previouslyReceived = goodsReceiptItemRepository.sumReceivedQuantityByPoItemId(poItem.getId());
            BigDecimal remainingPending = poItem.getOrderedQuantity().subtract(previouslyReceived);

            BigDecimal accepted = itemReq.getAcceptedQuantity();
            BigDecimal rejected = itemReq.getRejectedQuantity() != null ? itemReq.getRejectedQuantity() : BigDecimal.ZERO;
            BigDecimal currentReceived = accepted.add(rejected);

            if (currentReceived.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Current received quantity must be greater than zero.");
            }

            if (currentReceived.compareTo(remainingPending) > 0) {
                throw new BusinessException("Received quantity (" + currentReceived + ") exceeds remaining pending quantity (" + remainingPending + ") for item: " + poItem.getItem().getName());
            }

            BigDecimal pendingQuantity = remainingPending.subtract(currentReceived);

            GoodsReceiptItem grnItem = mapper.toItemEntity(itemReq);
            grnItem.setPoItemReference(poItem);
            grnItem.setItem(poItem.getItem());
            grnItem.setMaterialGrade(poItem.getMaterialGrade());
            grnItem.setOrderedQuantity(poItem.getOrderedQuantity());
            grnItem.setPreviouslyReceivedQuantity(previouslyReceived);
            grnItem.setCurrentReceivedQuantity(currentReceived);
            grnItem.setAcceptedQuantity(accepted);
            grnItem.setRejectedQuantity(rejected);
            grnItem.setPendingQuantity(pendingQuantity);

            grn.addItem(grnItem);

            // Track PO item status check
            BigDecimal totalReceivedIncludingCurrent = previouslyReceived.add(currentReceived);
            if (totalReceivedIncludingCurrent.compareTo(poItem.getOrderedQuantity()) < 0) {
                allPoItemsFullyReceived = false;
            }
            if (totalReceivedIncludingCurrent.compareTo(BigDecimal.ZERO) > 0) {
                anyReceived = true;
            }
        }

        // Check other items in the PO that are not in this GRN
        for (PurchaseOrderItem poItem : po.getItems()) {
            boolean isAlreadyHandledInThisGrn = request.getItems().stream()
                    .anyMatch(r -> r.getPoItemReferenceId().equals(poItem.getId()));
            if (!isAlreadyHandledInThisGrn) {
                BigDecimal previouslyReceived = goodsReceiptItemRepository.sumReceivedQuantityByPoItemId(poItem.getId());
                if (previouslyReceived.compareTo(poItem.getOrderedQuantity()) < 0) {
                    allPoItemsFullyReceived = false;
                }
                if (previouslyReceived.compareTo(BigDecimal.ZERO) > 0) {
                    anyReceived = true;
                }
            }
        }

        // Save Goods Receipt Note
        GoodsReceipt savedGrn = repository.save(grn);

        // Update Stock and log Stock Transactions (Integration with Inventory)
        for (GoodsReceiptItem item : savedGrn.getItems()) {
            if (item.getAcceptedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                // Update stock using StockService
                stockService.addStock(item.getItem(), item.getMaterialGrade(), item.getAcceptedQuantity());
                
                // Record stock transaction using StockTransactionService
                stockTransactionService.createTransaction(
                        StockTransactionType.GOODS_RECEIPT,
                        item.getItem(),
                        item.getMaterialGrade(),
                        item.getAcceptedQuantity(),
                        savedGrn.getGrnNumber(),
                        savedGrn.getRemarks() != null ? savedGrn.getRemarks() : "Goods Receipt inward"
                );
            }
        }

        // Update Purchase Order status
        if (allPoItemsFullyReceived) {
            po.setStatus(PurchaseOrderStatus.FULLY_RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        purchaseOrderRepository.save(po);

        return mapper.toResponse(savedGrn);
    }

    @Override
    public GoodsReceiptResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt Note not found with ID: " + id));
    }

    @Override
    public List<GoodsReceiptResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<GoodsReceiptResponse> getByPurchaseOrderId(UUID purchaseOrderId) {
        return repository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private String generateGrnNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<GoodsReceipt> lastGrn = repository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastGrn.isPresent()) {
            String lastNumber = lastGrn.get().getGrnNumber();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 4) { // E.g., YT-GRN-2026-00001
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1
                }
            }
        }

        String companyPrefix = companyCode != null && !companyCode.trim().isEmpty() ? companyCode.trim() : "YT";
        return String.format("%s-GRN-%d-%05d", companyPrefix, year, nextNumber);
    }
}
