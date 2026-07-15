package com.kalibyte.YashTools.inventory.transaction.stocktake.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeLineResponse {
    private UUID id;
    
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private BigDecimal systemQuantity;
    private BigDecimal physicalQuantity;
    private BigDecimal differenceQuantity;
    private Boolean approved;
}
