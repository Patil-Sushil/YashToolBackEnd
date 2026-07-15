package com.kalibyte.YashTools.inventory.transaction.stockadjustment.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentRequest;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentResponse;

import java.util.UUID;

public interface StockAdjustmentService {
    StockAdjustmentResponse adjustStock(StockAdjustmentRequest request);
    StockAdjustmentResponse getAdjustmentById(UUID id);
    StockAdjustmentResponse getAdjustmentByNumber(String number);
    PageResponse<StockAdjustmentResponse> getAllAdjustments(int page, int size);
}
