package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request;

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
public class PurchaseInvoiceItemRequest {

    @NotNull(message = "Item ID is required")
    private UUID itemId;

    private UUID materialGradeId;

    @NotNull(message = "Invoice quantity is required")
    @DecimalMin(value = "0.0001", message = "Invoice quantity must be greater than zero")
    private BigDecimal invoiceQuantity;

    @NotNull(message = "Invoice rate is required")
    @DecimalMin(value = "0.0", message = "Invoice rate must be non-negative")
    private BigDecimal invoiceRate;

    private BigDecimal gst;
}
