package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.service;

import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptResponse;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptService {
    GoodsReceiptResponse create(GoodsReceiptRequest request);
    GoodsReceiptResponse getById(UUID id);
    List<GoodsReceiptResponse> getAll();
    List<GoodsReceiptResponse> getByPurchaseOrderId(UUID purchaseOrderId);
}
