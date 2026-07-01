package com.kalibyte.YashTools.quotation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuotationRequest {

    private LocalDate validUntil;
    private String remarks;
    private String termsAndConditions;
    private String paymentTerms;
    private String deliveryTerms;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal discountPercentage;

    @Valid
    private List<QuotationItemRequest> items;
}