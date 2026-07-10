package com.kalibyte.YashTools.quotation.dto.request;

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
public class CreateDirectQuotationRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private Boolean isUrgent = false;
    private LocalDate validUntil;
    private String remarks;
    private String internalNotes;
    private String termsAndConditions;
    private String paymentTerms;
    private String deliveryTerms;

    @NotEmpty(message = "Quotation must contain at least one item")
    @Valid
    private List<QuotationItemRequest> items;
}