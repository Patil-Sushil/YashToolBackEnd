package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionExecutionReportDTO {
    private String periodLabel;
    private Integer totalTargetQuantity;
    private Integer totalProducedQuantity;
    private Integer totalRejectedQuantity;
    private Integer totalReworkQuantity;
    private Integer totalDowntimeMinutes;
    private List<ProductionExecutionDetailDTO> details;
}
