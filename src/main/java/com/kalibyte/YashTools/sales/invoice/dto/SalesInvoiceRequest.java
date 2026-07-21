package com.kalibyte.YashTools.sales.invoice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesInvoiceRequest {
    @NotNull(message = "Work Order ID is required")
    private UUID workOrderId;

    @Builder.Default
    private Boolean isInterstate = false;

    private String remarks;

    @NotEmpty(message = "Items to invoice are required")
    private List<SalesInvoiceItemRequest> items;
}
