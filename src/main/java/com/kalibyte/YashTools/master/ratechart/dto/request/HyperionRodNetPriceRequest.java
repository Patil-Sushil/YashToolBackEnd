package com.kalibyte.YashTools.master.ratechart.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HyperionRodNetPriceRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name must not exceed 200 characters")
    private String item;

    @NotNull(message = "Rate for K40UF/H10F is required")
    @DecimalMin(value = "0.01", message = "K40UF/H10F rate must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid K40UF/H10F rate format")
    private BigDecimal k40ufH10f;

    @NotNull(message = "Rate for AM70/DM80 is required")
    @DecimalMin(value = "0.01", message = "AM70/DM80 rate must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid AM70/DM80 rate format")
    private BigDecimal am70Dm80;

    @NotNull(message = "Rate for PN90 is required")
    @DecimalMin(value = "0.01", message = "PN90 rate must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid PN90 rate format")
    private BigDecimal pn90;

    @NotNull(message = "Rate for GP10/K10F is required")
    @DecimalMin(value = "0.01", message = "GP10/K10F rate must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid GP10/K10F rate format")
    private BigDecimal gp10K10f;
}