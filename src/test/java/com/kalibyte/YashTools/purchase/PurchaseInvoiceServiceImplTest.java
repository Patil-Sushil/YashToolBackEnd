package com.kalibyte.YashTools.purchase;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository.GoodsReceiptRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceItemRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoiceItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.mapper.PurchaseInvoiceMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.service.impl.PurchaseInvoiceServiceImpl;
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

class PurchaseInvoiceServiceImplTest {

    @Mock
    private PurchaseInvoiceRepository repository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PurchaseInvoiceMapper mapper;

    @InjectMocks
    private PurchaseInvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_Success_WithWarnings() {
        UUID poId = UUID.randomUUID();
        UUID grnId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        PurchaseInvoiceRequest request = new PurchaseInvoiceRequest();
        request.setSupplierInvoiceNumber("SUP-INV-001");
        request.setInvoiceDate(LocalDate.now());
        request.setPurchaseOrderId(poId);
        request.setGoodsReceiptIds(List.of(grnId));

        PurchaseInvoiceItemRequest itemReq = new PurchaseInvoiceItemRequest();
        itemReq.setItemId(itemId);
        itemReq.setInvoiceQuantity(BigDecimal.valueOf(8)); // Invoice says 8
        itemReq.setInvoiceRate(BigDecimal.valueOf(100));
        request.setItems(List.of(itemReq));

        Vendor vendor = new Vendor();
        vendor.setVendorName("Vendor 1");

        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setVendor(vendor);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Item 1");
        item.setSku("SKU-1");

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setItem(item);
        poItem.setOrderedQuantity(BigDecimal.TEN); // PO ordered 10
        po.addItem(poItem);

        GoodsReceipt grn = new GoodsReceipt();
        grn.setId(grnId);
        grn.setPurchaseOrder(po);

        GoodsReceiptItem grnItem = new GoodsReceiptItem();
        grnItem.setItem(item);
        grnItem.setAcceptedQuantity(BigDecimal.valueOf(7)); // GRN received 7
        grn.addItem(grnItem);

        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPurchaseOrder(po);
        invoice.setVendor(vendor);
        invoice.setGoodsReceipts(List.of(grn));

        PurchaseInvoiceItem invItem = new PurchaseInvoiceItem();
        invItem.setItem(item);
        invItem.setInvoiceQuantity(BigDecimal.valueOf(8));
        invItem.setInvoiceRate(BigDecimal.valueOf(100));
        invoice.addItem(invItem);

        when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(po));
        when(goodsReceiptRepository.findById(grnId)).thenReturn(Optional.of(grn));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mapper.toEntity(any())).thenReturn(invoice);
        when(mapper.toItemEntity(any())).thenReturn(invItem);
        when(repository.save(any())).thenReturn(invoice);
        
        PurchaseInvoiceResponse responseDto = PurchaseInvoiceResponse.builder()
                .invoiceNumber("PINV-01")
                .supplierInvoiceNumber("SUP-INV-001")
                .build();
        when(mapper.toResponse(any())).thenReturn(responseDto);

        PurchaseInvoiceResponse response = service.create(request);

        assertNotNull(response);
        assertNotNull(response.getWarnings());
        
        // Assert discrepancy warnings are generated
        assertTrue(response.getWarnings().size() > 0);
        assertTrue(response.getWarnings().stream().anyMatch(w -> w.contains("PO (10)") && w.contains("GRNs (7)")));
        assertTrue(response.getWarnings().stream().anyMatch(w -> w.contains("GRNs (7)") && w.contains("quantity (8)")));

        verify(repository, times(1)).save(any());
    }
}
