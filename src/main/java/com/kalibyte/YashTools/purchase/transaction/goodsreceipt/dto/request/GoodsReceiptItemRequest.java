package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItemRequest {

    @NotNull(message = "PO Item Reference ID is required")
    private UUID poItemReferenceId;

    @NotNull(message = "Accepted quantity is required")
    @DecimalMin(value = "0.0", message = "Accepted quantity must be non-negative")
    private BigDecimal acceptedQuantity;

    private BigDecimal rejectedQuantity;
}
