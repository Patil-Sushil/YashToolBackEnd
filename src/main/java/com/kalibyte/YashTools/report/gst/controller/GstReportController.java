package com.kalibyte.YashTools.report.gst.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.report.dto.DateRangePreset;
import com.kalibyte.YashTools.report.dto.DateRangeRequest;
import com.kalibyte.YashTools.report.util.DateRangeResolver;
import com.kalibyte.YashTools.report.gst.dto.Gstr1ReportDTO;
import com.kalibyte.YashTools.report.gst.dto.Gstr2ReportDTO;
import com.kalibyte.YashTools.report.gst.dto.Gstr3bReportDTO;
import com.kalibyte.YashTools.report.gst.service.GstReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/gst")
@RequiredArgsConstructor
@Tag(name = "GST Reports", description = "APIs for GST compliance, reporting, and return calculations (accessible by CA and ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('CA', 'ADMIN')")
public class GstReportController {

    private final GstReportService gstReportService;

    @GetMapping("/gstr1")
    @Operation(summary = "Get GSTR-1 outward supplies (sales) report")
    @LoggableAction(value = "Retrieve GSTR-1 Sales Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<Gstr1ReportDTO>> getGstr1Report(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(gstReportService.getGstr1Report(range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/gstr2")
    @Operation(summary = "Get GSTR-2 inward supplies (purchases ITC) report")
    @LoggableAction(value = "Retrieve GSTR-2 Purchases Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<Gstr2ReportDTO>> getGstr2Report(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(gstReportService.getGstr2Report(range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/gstr3b")
    @Operation(summary = "Get GSTR-3B summary return report")
    @LoggableAction(value = "Retrieve GSTR-3B Return Summary", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<Gstr3bReportDTO>> getGstr3bReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(gstReportService.getGstr3bReport(range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/gstr1/export")
    @Operation(summary = "Export GSTR-1 outward supplies report to Excel")
    @LoggableAction(value = "Export GSTR-1 Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportGstr1(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        Gstr1ReportDTO report = gstReportService.getGstr1Report(range.startDate(), range.endDate(), label);
        
        byte[] excelContent = gstReportService.exportGstr1ToExcel(report);
        String filename = "gstr1_report_" + (preset != null ? preset.name().toLowerCase() : range.startDate().toString()) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @GetMapping("/gstr2/export")
    @Operation(summary = "Export GSTR-2 inward supplies report to Excel")
    @LoggableAction(value = "Export GSTR-2 Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportGstr2(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        Gstr2ReportDTO report = gstReportService.getGstr2Report(range.startDate(), range.endDate(), label);
        
        byte[] excelContent = gstReportService.exportGstr2ToExcel(report);
        String filename = "gstr2_report_" + (preset != null ? preset.name().toLowerCase() : range.startDate().toString()) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @GetMapping("/gstr3b/export")
    @Operation(summary = "Export GSTR-3B summary return report to Excel")
    @LoggableAction(value = "Export GSTR-3B Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportGstr3b(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        Gstr3bReportDTO report = gstReportService.getGstr3bReport(range.startDate(), range.endDate(), label);
        
        byte[] excelContent = gstReportService.exportGstr3bToExcel(report);
        String filename = "gstr3b_report_" + (preset != null ? preset.name().toLowerCase() : range.startDate().toString()) + ".xlsx";
        
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
