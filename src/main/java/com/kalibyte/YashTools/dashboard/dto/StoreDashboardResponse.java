package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDashboardResponse {
    private long totalItemsCount;
    private long lowStockItemsCount;
    private long pendingGoodsReceipts;
    private BigDecimal totalStockValue;
    private List<StockTransactionDto> recentStockTransactions;
}
