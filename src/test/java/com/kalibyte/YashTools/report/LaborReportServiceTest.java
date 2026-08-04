package com.kalibyte.YashTools.report;

import com.kalibyte.YashTools.labors.attendance.entity.Attendance;
import com.kalibyte.YashTools.labors.attendance.repository.AttendanceRepository;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import com.kalibyte.YashTools.report.dto.LaborDetailedReportDTO;
import com.kalibyte.YashTools.report.dto.LaborExpenseReportDTO;
import com.kalibyte.YashTools.report.service.LaborReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LaborReportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    private LaborReportService laborReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        laborReportService = new LaborReportService(attendanceRepository);
    }

    @Test
    void getDetailedReport_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        Laborer laborer = Laborer.builder().id(1L).name("Laborer John").build();
        Attendance attendance = Attendance.builder()
                .laborer(laborer)
                .workDate(LocalDate.now())
                .hoursWorked(BigDecimal.valueOf(8))
                .earnedAmount(BigDecimal.valueOf(1000))
                .build();

        when(attendanceRepository.findByWorkDateBetween(start, end)).thenReturn(List.of(attendance));

        List<LaborDetailedReportDTO> detailedReports = laborReportService.getDetailedReport(start, end);

        assertNotNull(detailedReports);
        assertEquals(1, detailedReports.size());
        
        LaborDetailedReportDTO report = detailedReports.getFirst();
        assertEquals("Laborer John", report.getLaborerName());
        assertEquals(BigDecimal.valueOf(8), report.getTotalHours());
        assertEquals(BigDecimal.valueOf(1000), report.getTotalEarned());
        assertEquals(1, report.getAttendanceDetails().size());
    }

    @Test
    void getAggregatedExpensesForPeriod_Success() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();

        Laborer laborer1 = Laborer.builder().id(1L).name("Laborer John").build();
        Laborer laborer2 = Laborer.builder().id(2L).name("Laborer Mary").build();

        Attendance attendance1 = Attendance.builder()
                .laborer(laborer1)
                .workDate(LocalDate.now())
                .hoursWorked(BigDecimal.valueOf(8))
                .earnedAmount(BigDecimal.valueOf(1000))
                .build();

        Attendance attendance2 = Attendance.builder()
                .laborer(laborer2)
                .workDate(LocalDate.now())
                .hoursWorked(BigDecimal.valueOf(6))
                .earnedAmount(BigDecimal.valueOf(800))
                .build();

        when(attendanceRepository.findByWorkDateBetween(start, end)).thenReturn(List.of(attendance1, attendance2));

        LaborExpenseReportDTO summary = laborReportService.getAggregatedExpensesForPeriod(start, end, "SUMMARY_LABEL");

        assertNotNull(summary);
        assertEquals("SUMMARY_LABEL", summary.getPeriod());
        assertEquals(BigDecimal.valueOf(14), summary.getTotalHours());
        assertEquals(BigDecimal.valueOf(1800), summary.getTotalLaborCost());
        assertEquals(2, summary.getTotalWorkers());
    }
}
