package com.kalibyte.YashTools.inventory.transaction.stocktake.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeRequest;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeResponse;

import java.util.UUID;

public interface StockTakeService {
    StockTakeResponse createStockTake(StockTakeRequest request);
    StockTakeResponse getStockTakeById(UUID id);
    StockTakeResponse getStockTakeByNumber(String number);
    PageResponse<StockTakeResponse> getAllStockTakes(int page, int size);
    StockTakeResponse completeStockTake(UUID id);
    StockTakeResponse approveStockTake(UUID id);
}
