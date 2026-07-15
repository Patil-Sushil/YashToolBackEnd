package com.kalibyte.YashTools.inventory.master.materialgrade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialGradeRequest {

    @NotBlank(message = "Material grade name is required")
    @Size(min = 2, max = 100, message = "Material grade name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Material grade code is required")
    @Size(min = 2, max = 50, message = "Material grade code must be between 2 and 50 characters")
    private String code;

    private String description;
}
