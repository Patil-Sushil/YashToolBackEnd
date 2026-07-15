package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.service;

import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.UpdatePurchaseInvoiceStatusRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseInvoiceService {
    PurchaseInvoiceResponse create(PurchaseInvoiceRequest request);
    PurchaseInvoiceResponse getById(UUID id);
    List<PurchaseInvoiceResponse> getAll();
    PurchaseInvoiceResponse updateStatus(UUID id, UpdatePurchaseInvoiceStatusRequest request);
}
