package com.kalibyte.YashTools.inventory.transaction.stock.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.transaction.stock.dto.StockResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface StockService {
    StockResponse getStock(UUID itemId, UUID materialGradeId);
    PageResponse<StockResponse> getAllStocks(int page, int size);
    
    // Internal API used by other services for transaction processing
    void addStock(Item item, MaterialGrade materialGrade, BigDecimal quantity);
    void deductStock(Item item, MaterialGrade materialGrade, BigDecimal quantity);
}
