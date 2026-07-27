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
public class QualityInspectionReportDTO {
    private String periodLabel;
    private long totalInspectedCount;
    private Integer totalAcceptedQuantity;
    private Integer totalRejectedQuantity;
    private Integer totalReworkQuantity;
    private Double passRate;
    private Double rejectRate;
    private Double reworkRate;
    private List<QualityInspectionDetailDTO> details;
}
