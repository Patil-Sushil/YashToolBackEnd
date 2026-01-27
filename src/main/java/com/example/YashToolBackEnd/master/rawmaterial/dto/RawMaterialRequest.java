package com.example.YashToolBackEnd.master.rawmaterial.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RawMaterialRequest {
    @NotBlank
    private String name;

    @NotBlank
    private Double rate;

}
