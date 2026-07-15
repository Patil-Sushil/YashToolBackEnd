package com.kalibyte.YashTools.inventory.transaction.stocktransaction.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.dto.StockTransactionResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface StockTransactionService {
    StockTransactionResponse getTransactionById(UUID id);
    PageResponse<StockTransactionResponse> searchTransactions(StockTransactionType type, UUID itemId, String referenceNumber, int page, int size);
    
    // Internal API used when stock transitions are performed
    void createTransaction(
            StockTransactionType type,
            Item item,
            MaterialGrade materialGrade,
            BigDecimal quantity,
            String referenceNumber,
            String remarks
    );
}
