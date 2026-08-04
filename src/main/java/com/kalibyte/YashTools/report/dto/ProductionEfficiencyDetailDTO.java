package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionEfficiencyDetailDTO {
    private String orderType;
    private long jobCardCount;
    private Integer targetQuantity;
    private Integer producedQuantity;
    private Integer rejectedQuantity;
    private Double efficiency;
}
