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
public class LaborDetailedReportDTO {
    private String laborerName;
    private BigDecimal totalHours;
    private BigDecimal totalEarned;
    private List<LaborAttendanceReportDTO> attendanceDetails;
}
