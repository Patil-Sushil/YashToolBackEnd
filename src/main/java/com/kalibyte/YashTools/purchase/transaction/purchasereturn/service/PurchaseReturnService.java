package com.kalibyte.YashTools.purchase.transaction.purchasereturn.service;

import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request.PurchaseReturnRequest;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response.PurchaseReturnResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseReturnService {
    PurchaseReturnResponse create(PurchaseReturnRequest request);
    PurchaseReturnResponse getById(UUID id);
    List<PurchaseReturnResponse> getAll();
}
