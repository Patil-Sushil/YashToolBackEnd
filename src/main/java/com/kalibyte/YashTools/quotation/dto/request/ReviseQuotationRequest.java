package com.kalibyte.YashTools.quotation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviseQuotationRequest {

    @NotNull(message = "Parent quotation id is required")
    private UUID parentQuotationId;

    @NotBlank(message = "Revision reason is mandatory for audit")
    private String revisionReason;

    private String revisionType;   // maps to RevisionType enum

    @Valid
    private List<QuotationItemRequest> items;

    private BigDecimal discountPercentage;
    private String termsAndConditions;
}