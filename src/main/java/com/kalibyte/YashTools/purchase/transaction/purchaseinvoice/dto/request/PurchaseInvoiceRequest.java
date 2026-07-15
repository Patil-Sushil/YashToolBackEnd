package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceRequest {

    @NotBlank(message = "Supplier invoice number is required")
    private String supplierInvoiceNumber;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotNull(message = "Purchase Order ID is required")
    private UUID purchaseOrderId;

    @NotEmpty(message = "At least one GRN must be mapped")
    private List<UUID> goodsReceiptIds;

    private String gstDetails;

    private BigDecimal freight;

    private BigDecimal otherCharges;

    @NotEmpty(message = "Invoice must contain at least one item")
    @Valid
    private List<PurchaseInvoiceItemRequest> items;
}
