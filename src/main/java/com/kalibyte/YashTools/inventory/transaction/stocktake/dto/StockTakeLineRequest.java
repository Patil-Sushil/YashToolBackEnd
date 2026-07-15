package com.kalibyte.YashTools.inventory.transaction.stocktake.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTakeLineRequest {

    @NotNull(message = "Item ID is required")
    private UUID itemId;

    private UUID materialGradeId;

    @NotNull(message = "Physical quantity is required")
    @DecimalMin(value = "0.0", message = "Physical quantity cannot be negative")
    private BigDecimal physicalQuantity;
}
