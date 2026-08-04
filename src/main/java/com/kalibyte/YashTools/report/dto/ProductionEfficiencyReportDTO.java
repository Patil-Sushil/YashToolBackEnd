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
public class ProductionEfficiencyReportDTO {
    private String periodLabel;
    private Double overallEfficiency;
    private List<ProductionEfficiencyDetailDTO> details;
}
