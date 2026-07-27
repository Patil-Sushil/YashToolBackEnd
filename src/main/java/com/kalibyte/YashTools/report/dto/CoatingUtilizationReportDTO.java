package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoatingUtilizationReportDTO {
    private String periodLabel;
    private Integer totalCoatedQuantity;
    private BigDecimal totalEstimatedCost;
    private List<CoatingUtilizationDetailDTO> details;
}
