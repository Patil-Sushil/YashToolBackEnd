package com.kalibyte.YashTools.master.rawmaterial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialResponse {
    private Long id;
    private String name;
    private Double rate;
    private Boolean active;
}
