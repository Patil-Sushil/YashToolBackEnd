package com.example.YashToolBackEnd.master.coating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoatingRequest {
    @NotBlank
    private String name;

    @NotNull
    private Double rate;
}
