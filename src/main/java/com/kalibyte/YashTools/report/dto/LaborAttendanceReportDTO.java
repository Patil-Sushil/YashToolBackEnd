package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaborAttendanceReportDTO {
    private LocalDate workDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Integer pieceCompleted;
    private BigDecimal hoursWorked;
    private BigDecimal earnedAmount;
}
