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
public class LaborExpenseReportDTO {
    private String period;
    private BigDecimal totalHours;
    private BigDecimal totalLaborCost;
    private long totalWorkers;
    private List<LaborDetailedReportDTO> laborDetails;
}
