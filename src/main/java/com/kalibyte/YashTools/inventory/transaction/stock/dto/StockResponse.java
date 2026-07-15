package com.kalibyte.YashTools.inventory.transaction.stock.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private UUID id;
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private BigDecimal quantity;
}
