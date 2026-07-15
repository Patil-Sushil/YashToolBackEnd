package com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnItemRequest {

    @NotNull(message = "Item ID is required")
    private UUID itemId;

    private UUID materialGradeId;

    @NotNull(message = "Return quantity is required")
    @DecimalMin(value = "0.0001", message = "Return quantity must be greater than zero")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", message = "Rate must be non-negative")
    private BigDecimal rate;

    private String remarks;
}
