package com.kalibyte.YashTools.purchase.master.purchasetype.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTypeRequest {

    @NotBlank(message = "Purchase type code is required")
    private String code;

    @NotBlank(message = "Purchase type name is required")
    private String name;

    private String description;
    
    private Boolean active;
}
