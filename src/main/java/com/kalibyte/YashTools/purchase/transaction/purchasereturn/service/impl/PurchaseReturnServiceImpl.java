package com.kalibyte.YashTools.purchase.transaction.purchasereturn.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request.PurchaseReturnRequest;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response.PurchaseReturnResponse;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.entity.PurchaseReturn;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.entity.PurchaseReturnItem;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.mapper.PurchaseReturnMapper;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.repository.PurchaseReturnRepository;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.service.PurchaseReturnService;
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
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository repository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ItemRepository itemRepository;
    private final MaterialGradeRepository materialGradeRepository;
    private final StockService stockService;
    private final StockTransactionService stockTransactionService;
    private final PurchaseReturnMapper mapper;

    @Override
    @Transactional
    @LoggableAction(value = "Create Purchase Return", action = AuditAction.PURCHASE_RETURN_CREATED, entityType = "PURCHASE_RETURN")
    public PurchaseReturnResponse create(PurchaseReturnRequest request) {
        log.info("Creating Purchase Return against GRN: {}", request.getGoodsReceiptId());

        GoodsReceipt grn = goodsReceiptRepository.findById(request.getGoodsReceiptId())
                .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt Note not found with ID: " + request.getGoodsReceiptId()));

        PurchaseOrder po = grn.getPurchaseOrder();
        PurchaseInvoice pi = null;
        if (request.getPurchaseInvoiceId() != null) {
            pi = purchaseInvoiceRepository.findById(request.getPurchaseInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Invoice not found with ID: " + request.getPurchaseInvoiceId()));

            if (!pi.getPurchaseOrder().getId().equals(po.getId())) {
                throw new BusinessException("Purchase Invoice is not associated with the same Purchase Order.");
            }
        }

        PurchaseReturn pReturn = mapper.toEntity(request);
        pReturn.setGoodsReceipt(grn);
        pReturn.setPurchaseOrder(po);
        pReturn.setVendor(po.getVendor());
        pReturn.setPurchaseInvoice(pi);
        if (po.getCompany() != null) {
            pReturn.setCompany(po.getCompany());
        }

        if (request.getReturnDate() == null) {
            pReturn.setReturnDate(LocalDate.now());
        }

        pReturn.setReturnNumber(generateReturnNumber(pReturn.getCompany() != null ? pReturn.getCompany().getCode() : "YT"));
        pReturn.setItems(new ArrayList<>());

        for (var itemReq : request.getItems()) {
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemReq.getItemId()));

            MaterialGrade materialGrade = null;
            if (itemReq.getMaterialGradeId() != null) {
                materialGrade = materialGradeRepository.findById(itemReq.getMaterialGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Material Grade not found with ID: " + itemReq.getMaterialGradeId()));
            }

            // Find original GRN Item to validate quantities
            final MaterialGrade finalGrade = materialGrade;
            GoodsReceiptItem grnItem = grn.getItems().stream()
                    .filter(gri -> gri.getItem().getId().equals(item.getId()) &&
                            ((gri.getMaterialGrade() == null && finalGrade == null) ||
                             (gri.getMaterialGrade() != null && finalGrade != null && gri.getMaterialGrade().getId().equals(finalGrade.getId()))))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Item " + item.getName() + " was not received in the original GRN " + grn.getGrnNumber()));

            if (itemReq.getQuantity().compareTo(grnItem.getAcceptedQuantity()) > 0) {
                throw new BusinessException("Return quantity (" + itemReq.getQuantity() + ") cannot exceed GRN accepted quantity (" + grnItem.getAcceptedQuantity() + ") for item: " + item.getName());
            }

            PurchaseReturnItem returnItem = mapper.toItemEntity(itemReq);
            returnItem.setItem(item);
            returnItem.setMaterialGrade(materialGrade);

            pReturn.addItem(returnItem);
        }

        PurchaseReturn saved = repository.save(pReturn);

        // Deduct Stock and log Stock Transactions (Integration with Inventory)
        for (PurchaseReturnItem item : saved.getItems()) {
            if (item.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                // Deduct stock using StockService
                stockService.deductStock(item.getItem(), item.getMaterialGrade(), item.getQuantity());

                // Record stock transaction using StockTransactionService
                stockTransactionService.createTransaction(
                        StockTransactionType.PURCHASE_RETURN,
                        item.getItem(),
                        item.getMaterialGrade(),
                        item.getQuantity(),
                        saved.getReturnNumber(),
                        saved.getRemarks() != null ? saved.getRemarks() : "Purchase Return"
                );
            }
        }

        return mapper.toResponse(saved);
    }

    @Override
    public PurchaseReturnResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Return not found with ID: " + id));
    }

    @Override
    public List<PurchaseReturnResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    private String generateReturnNumber(String companyCode) {
        int year = LocalDate.now().getYear();
        Optional<PurchaseReturn> lastReturn = repository.findTopByOrderByCreatedAtDesc();
        long nextNumber = 1;

        if (lastReturn.isPresent()) {
            String lastNumber = lastReturn.get().getReturnNumber();
            String[] parts = lastNumber.split("-");
            if (parts.length >= 4) { // E.g., YT-PRT-2026-00001
                try {
                    nextNumber = Long.parseLong(parts[parts.length - 1]) + 1;
                } catch (NumberFormatException e) {
                    // Keep 1
                }
            }
        }

        String companyPrefix = companyCode != null && !companyCode.trim().isEmpty() ? companyCode.trim() : "YT";
        return String.format("%s-PRT-%d-%05d", companyPrefix, year, nextNumber);
    }
}
