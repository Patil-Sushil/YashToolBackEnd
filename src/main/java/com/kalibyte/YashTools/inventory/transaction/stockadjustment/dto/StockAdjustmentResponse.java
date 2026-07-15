package com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto;

import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentReason;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentResponse {
    private UUID id;
    private String adjustmentNumber;
    
    private UUID itemId;
    private String itemName;
    private String itemSku;
    
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    
    private BigDecimal quantity;
    private StockAdjustmentType adjustmentType;
    private StockAdjustmentReason reason;
    private String remarks;
    
    private LocalDateTime createdAt;
    private String createdBy;
}
