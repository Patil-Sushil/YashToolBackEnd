package com.kalibyte.YashTools.purchase.master.paymentterms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTermsRequest {

    @NotBlank(message = "Payment terms code is required")
    private String code;

    @NotBlank(message = "Payment terms name is required")
    private String name;

    @NotNull(message = "Number of days is required")
    private Integer numberOfDays;

    private String description;
    
    private Boolean active;
}
