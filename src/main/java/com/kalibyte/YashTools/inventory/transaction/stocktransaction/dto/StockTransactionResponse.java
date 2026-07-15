package com.kalibyte.YashTools.inventory.transaction.stocktransaction.dto;

import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {
    private UUID id;
    private StockTransactionType transactionType;
    
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private BigDecimal quantity;
    private String referenceNumber;
    private String remarks;
    
    private LocalDateTime createdAt;
    private String createdBy;
}
