package com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    private LocalDate poDate;

    private LocalDate expectedDeliveryDate;

    private UUID paymentTermsId;

    private UUID purchaseTypeId;

    private String remarks;

    @NotEmpty(message = "Purchase order must contain at least one item")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}
