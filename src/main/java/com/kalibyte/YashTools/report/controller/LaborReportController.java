package com.kalibyte.YashTools.report.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.report.dto.DateRangePreset;
import com.kalibyte.YashTools.report.dto.DateRangeRequest;
import com.kalibyte.YashTools.report.dto.LaborDetailedReportDTO;
import com.kalibyte.YashTools.report.dto.LaborExpenseReportDTO;
import com.kalibyte.YashTools.report.service.LaborReportService;
import com.kalibyte.YashTools.report.util.DateRangeResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/labor-reports")
@RequiredArgsConstructor
@Tag(name = "Labor Reports", description = "APIs for labor expense reporting and analytics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class LaborReportController {

    private final LaborReportService laborReportService;

    @GetMapping("/summary")
    @Operation(summary = "Get labor expense summary for a date range/preset")
    @LoggableAction(value = "Retrieve Labor Expense Summary", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<LaborExpenseReportDTO>> getSummary(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(laborReportService.getAggregatedExpensesForPeriod(range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly labor expense report", description = "Only accessible by ADMIN")
    @LoggableAction(value = "Retrieve Weekly Labor Expense Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<LaborExpenseReportDTO>> getWeeklyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(laborReportService.getWeeklyReport(date)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly labor expense report", description = "Only accessible by ADMIN")
    @LoggableAction(value = "Retrieve Monthly Labor Expense Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<LaborExpenseReportDTO>> getMonthlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(laborReportService.getMonthlyReport(date)));
    }

    @GetMapping("/yearly/{year}")
    @Operation(summary = "Get yearly labor expense report", description = "Only accessible by ADMIN")
    @LoggableAction(value = "Retrieve Yearly Labor Expense Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<LaborExpenseReportDTO>> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(ApiResponse.success(laborReportService.getYearlyReport(year)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export labor expense reports to Excel", description = "Only accessible by ADMIN")
    @LoggableAction(value = "Export Labor Expense Reports to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportReports(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        List<LaborDetailedReportDTO> reports = laborReportService.getDetailedReport(range.startDate(), range.endDate());
        
        byte[] excelContent = laborReportService.exportToExcel(reports);
        
        String filename = "labor_expense_report_" + (preset != null ? preset.name().toLowerCase() : range.startDate().toString()) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    private DateRangeResolver.DateRange resolveRange(DateRangePreset preset, LocalDate start, LocalDate end) {
        DateRangeRequest request = DateRangeRequest.builder()
                .preset(preset)
                .startDate(start)
                .endDate(end)
                .build();
        return DateRangeResolver.resolve(request);
    }
}
