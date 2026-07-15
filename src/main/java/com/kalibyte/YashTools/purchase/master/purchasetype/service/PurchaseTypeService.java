package com.kalibyte.YashTools.purchase.master.purchasetype.service;

import com.kalibyte.YashTools.purchase.master.purchasetype.dto.request.PurchaseTypeRequest;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.response.PurchaseTypeResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseTypeService {
    PurchaseTypeResponse create(PurchaseTypeRequest request);
    PurchaseTypeResponse getById(UUID id);
    List<PurchaseTypeResponse> getAll();
    PurchaseTypeResponse update(UUID id, PurchaseTypeRequest request);
    void delete(UUID id);
}
