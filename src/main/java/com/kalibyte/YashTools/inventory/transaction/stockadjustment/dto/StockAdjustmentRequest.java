package com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto;

import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentReason;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Item ID is required")
    private UUID itemId;

    private UUID materialGradeId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Adjustment Type is required")
    private StockAdjustmentType adjustmentType;

    @NotNull(message = "Reason is required")
    private StockAdjustmentReason reason;

    private String remarks;
}
