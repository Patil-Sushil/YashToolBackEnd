package com.example.YashToolBackEnd.master.rawmaterial.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RawMaterialResponse {
    private Long id;
    private String name;
    private Double rate;
    private Boolean active;
}
