package com.kalibyte.YashTools.master.rawmaterial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialRequest {

    @NotBlank(message = "Raw material name is required")
    private String name;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be greater than zero")
    private Double rate;
}