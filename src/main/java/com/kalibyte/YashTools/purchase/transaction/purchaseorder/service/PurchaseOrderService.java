package com.kalibyte.YashTools.purchase.transaction.purchaseorder.service;

import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.UpdatePurchaseOrderStatusRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderService {
    PurchaseOrderResponse create(PurchaseOrderRequest request);
    PurchaseOrderResponse getById(UUID id);
    List<PurchaseOrderResponse> getAll();
    PurchaseOrderResponse update(UUID id, PurchaseOrderRequest request);
    PurchaseOrderResponse updateStatus(UUID id, UpdatePurchaseOrderStatusRequest request);
    void delete(UUID id);


    List<PurchaseOrderResponse> searchPurchaseOrders(String query);

}
