package com.kalibyte.YashTools.master.rawmaterial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialRequest {
    @NotBlank
    private String name;

    @NotBlank
    private Double rate;

}
