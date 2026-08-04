package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionDto {
    private String transactionType;
    private String itemName;
    private String itemSku;
    private BigDecimal quantity;
    private String referenceNumber;
    private LocalDateTime timestamp;
}
