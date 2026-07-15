package com.kalibyte.YashTools.purchase;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptItemRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.mapper.GoodsReceiptMapper;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptItemRepository;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptRepository;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.service.impl.GoodsReceiptServiceImpl;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoodsReceiptServiceImplTest {

    @Mock
    private GoodsReceiptRepository repository;
    @Mock
    private GoodsReceiptItemRepository goodsReceiptItemRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private StockService stockService;
    @Mock
    private StockTransactionService stockTransactionService;
    @Mock
    private GoodsReceiptMapper mapper;

    @InjectMocks
    private GoodsReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_Success() {
        UUID poId = UUID.randomUUID();
        UUID poItemId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        GoodsReceiptRequest request = new GoodsReceiptRequest();
        request.setPurchaseOrderId(poId);
        request.setGrnDate(LocalDate.now());
        
        GoodsReceiptItemRequest itemReq = new GoodsReceiptItemRequest();
        itemReq.setPoItemReferenceId(poItemId);
        itemReq.setAcceptedQuantity(BigDecimal.valueOf(5));
        itemReq.setRejectedQuantity(BigDecimal.ZERO);
        request.setItems(List.of(itemReq));

        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setStatus(PurchaseOrderStatus.APPROVED);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Item 1");

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setId(poItemId);
        poItem.setItem(item);
        poItem.setOrderedQuantity(BigDecimal.TEN);
        po.addItem(poItem);

        GoodsReceipt grn = new GoodsReceipt();
        grn.setPurchaseOrder(po);
        grn.setGrnNumber("YT-GRN-2026-00001");

        GoodsReceiptItem grnItem = new GoodsReceiptItem();
        grnItem.setPoItemReference(poItem);
        grnItem.setItem(item);
        grnItem.setAcceptedQuantity(BigDecimal.valueOf(5));
        grnItem.setRejectedQuantity(BigDecimal.ZERO);
        grnItem.setCurrentReceivedQuantity(BigDecimal.valueOf(5));
        grn.addItem(grnItem);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(goodsReceiptItemRepository.sumReceivedQuantityByPoItemId(poItemId)).thenReturn(BigDecimal.ZERO);
        when(mapper.toEntity(any())).thenReturn(grn);
        when(mapper.toItemEntity(any())).thenReturn(grnItem);
        when(repository.save(any())).thenReturn(grn);
        when(mapper.toResponse(any())).thenReturn(GoodsReceiptResponse.builder().grnNumber("YT-GRN-2026-00001").build());

        GoodsReceiptResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("YT-GRN-2026-00001", response.getGrnNumber());
        verify(stockService, times(1)).addStock(item, null, BigDecimal.valueOf(5));
        verify(stockTransactionService, times(1)).createTransaction(
                eq(StockTransactionType.GOODS_RECEIPT), eq(item), eq(null), eq(BigDecimal.valueOf(5)), eq("YT-GRN-2026-00001"), any());
        
        // PO status should become Partially Received
        assertEquals(PurchaseOrderStatus.PARTIALLY_RECEIVED, po.getStatus());
        verify(purchaseOrderRepository, times(1)).save(po);
    }

    @Test
    void create_OverReceive_ThrowsException() {
        UUID poId = UUID.randomUUID();
        UUID poItemId = UUID.randomUUID();

        GoodsReceiptRequest request = new GoodsReceiptRequest();
        request.setPurchaseOrderId(poId);
        
        GoodsReceiptItemRequest itemReq = new GoodsReceiptItemRequest();
        itemReq.setPoItemReferenceId(poItemId);
        itemReq.setAcceptedQuantity(BigDecimal.valueOf(15)); // Exceeds PO quantity of 10
        itemReq.setRejectedQuantity(BigDecimal.ZERO);
        request.setItems(List.of(itemReq));

        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setStatus(PurchaseOrderStatus.APPROVED);

        Item item = new Item();
        item.setName("Item 1");

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setId(poItemId);
        poItem.setItem(item);
        poItem.setOrderedQuantity(BigDecimal.TEN);
        po.addItem(poItem);

        GoodsReceipt grn = new GoodsReceipt();
        grn.setPurchaseOrder(po);

        GoodsReceiptItem grnItem = new GoodsReceiptItem();
        grnItem.setPoItemReference(poItem);
        grnItem.setItem(item);
        grnItem.setAcceptedQuantity(BigDecimal.valueOf(15));

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(goodsReceiptItemRepository.sumReceivedQuantityByPoItemId(poItemId)).thenReturn(BigDecimal.ZERO);
        when(mapper.toEntity(any())).thenReturn(grn);
        when(mapper.toItemEntity(any())).thenReturn(grnItem);

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }
}
