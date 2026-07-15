package com.kalibyte.YashTools.purchase;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.item.repository.ItemRepository;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import com.kalibyte.YashTools.purchase.master.vendor.repository.VendorRepository;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderItemRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.UpdatePurchaseOrderStatusRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.mapper.PurchaseOrderMapper;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.service.impl.PurchaseOrderServiceImpl;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.validator.PurchaseOrderValidator;
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

class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository repository;
    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PurchaseOrderMapper mapper;
    @Mock
    private PurchaseOrderValidator validator;

    @InjectMocks
    private PurchaseOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_Success() {
        UUID vendorId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setVendorId(vendorId);
        request.setPoDate(LocalDate.now());
        
        PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest();
        itemReq.setItemId(itemId);
        itemReq.setOrderedQuantity(BigDecimal.TEN);
        itemReq.setRate(BigDecimal.valueOf(100));
        itemReq.setUnit("PCS");
        request.setItems(List.of(itemReq));

        Vendor vendor = new Vendor();
        vendor.setId(vendorId);
        vendor.setVendorName("Supplier Inc");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Carbide Inserts");
        item.setSku("CI-01");

        PurchaseOrder po = new PurchaseOrder();
        po.setVendor(vendor);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setItem(item);
        poItem.setOrderedQuantity(BigDecimal.TEN);
        poItem.setRate(BigDecimal.valueOf(100));

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(mapper.toEntity(any())).thenReturn(po);
        when(mapper.toItemEntity(any())).thenReturn(poItem);
        when(repository.save(any())).thenReturn(po);
        when(mapper.toResponse(any())).thenReturn(PurchaseOrderResponse.builder().status("DRAFT").build());

        PurchaseOrderResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    void updateStatus_Success() {
        UUID poId = UUID.randomUUID();
        UpdatePurchaseOrderStatusRequest request = new UpdatePurchaseOrderStatusRequest(PurchaseOrderStatus.APPROVED, "Approved by Manager");

        PurchaseOrder po = new PurchaseOrder();
        po.setId(poId);
        po.setStatus(PurchaseOrderStatus.DRAFT);

        when(repository.findById(poId)).thenReturn(Optional.of(po));
        when(repository.save(any())).thenReturn(po);
        when(mapper.toResponse(any())).thenReturn(PurchaseOrderResponse.builder().status("APPROVED").build());

        PurchaseOrderResponse response = service.updateStatus(poId, request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        verify(validator, times(1)).validateStatusTransition(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.APPROVED);
        verify(repository, times(1)).save(po);
    }
}
