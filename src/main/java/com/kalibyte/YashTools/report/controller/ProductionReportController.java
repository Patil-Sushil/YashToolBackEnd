package com.kalibyte.YashTools.report.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.production.planning.entity.enums.ShiftType;
import com.kalibyte.YashTools.production.tracking.entity.enums.InspectionResult;
import com.kalibyte.YashTools.report.dto.*;
import com.kalibyte.YashTools.report.service.ProductionReportService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/production-reports")
@RequiredArgsConstructor
@Tag(name = "Production Reports", description = "APIs for production planning, execution, QA, and Profit & Loss reports")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'FINANCE')")
public class ProductionReportController {

    private final ProductionReportService productionReportService;

    // --- Production Execution Reports ---

    @GetMapping("/execution")
    @Operation(summary = "Get production execution report")
    @LoggableAction(value = "Retrieve Production Execution Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<ProductionExecutionReportDTO>> getExecutionReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) UUID machineId,
            @RequestParam(required = false) ShiftType shift) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(productionReportService.getExecutionReport(
                range.startDate(), range.endDate(), operatorId, machineId, shift, label)));
    }

    @GetMapping("/execution/export-excel")
    @Operation(summary = "Export production execution report to Excel")
    @LoggableAction(value = "Export Production Execution Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportExecutionExcel(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) UUID machineId,
            @RequestParam(required = false) ShiftType shift) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProductionExecutionReportDTO data = productionReportService.getExecutionReport(
                range.startDate(), range.endDate(), operatorId, machineId, shift, label);
        
        byte[] bytes = productionReportService.exportExecutionReportToExcel(data);
        String filename = "production_execution_report_" + range.startDate().toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/execution/export-pdf")
    @Operation(summary = "Export production execution report to PDF")
    @LoggableAction(value = "Export Production Execution Report to PDF", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportExecutionPdf(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) UUID machineId,
            @RequestParam(required = false) ShiftType shift) throws Exception {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProductionExecutionReportDTO data = productionReportService.getExecutionReport(
                range.startDate(), range.endDate(), operatorId, machineId, shift, label);
        
        byte[] bytes = productionReportService.generatePdfReport("PRODUCTION_EXECUTION", data);
        String filename = "production_execution_report_" + range.startDate().toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // --- Quality Inspection Reports ---

    @GetMapping("/quality")
    @Operation(summary = "Get quality inspection report")
    @LoggableAction(value = "Retrieve Quality Inspection Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<QualityInspectionReportDTO>> getQualityReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String inspector,
            @RequestParam(required = false) InspectionResult result) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(productionReportService.getQualityReport(
                range.startDate(), range.endDate(), inspector, result, label)));
    }

    @GetMapping("/quality/export-excel")
    @Operation(summary = "Export quality inspection report to Excel")
    @LoggableAction(value = "Export Quality Inspection Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportQualityExcel(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String inspector,
            @RequestParam(required = false) InspectionResult result) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        QualityInspectionReportDTO data = productionReportService.getQualityReport(
                range.startDate(), range.endDate(), inspector, result, label);
        
        byte[] bytes = productionReportService.exportQualityReportToExcel(data);
        String filename = "quality_inspection_report_" + range.startDate().toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/quality/export-pdf")
    @Operation(summary = "Export quality inspection report to PDF")
    @LoggableAction(value = "Export Quality Inspection Report to PDF", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportQualityPdf(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String inspector,
            @RequestParam(required = false) InspectionResult result) throws Exception {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        QualityInspectionReportDTO data = productionReportService.getQualityReport(
                range.startDate(), range.endDate(), inspector, result, label);
        
        byte[] bytes = productionReportService.generatePdfReport("QUALITY_INSPECTION", data);
        String filename = "quality_inspection_report_" + range.startDate().toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // --- Coating Utilization Reports ---

    @GetMapping("/coating")
    @Operation(summary = "Get coating utilization report")
    @LoggableAction(value = "Retrieve Coating Utilization Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<CoatingUtilizationReportDTO>> getCoatingReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(productionReportService.getCoatingUtilizationReport(
                range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/coating/export-excel")
    @Operation(summary = "Export coating utilization report to Excel")
    @LoggableAction(value = "Export Coating Utilization Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportCoatingExcel(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        CoatingUtilizationReportDTO data = productionReportService.getCoatingUtilizationReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.exportCoatingReportToExcel(data);
        String filename = "coating_utilization_report_" + range.startDate().toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/coating/export-pdf")
    @Operation(summary = "Export coating utilization report to PDF")
    @LoggableAction(value = "Export Coating Utilization Report to PDF", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportCoatingPdf(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        CoatingUtilizationReportDTO data = productionReportService.getCoatingUtilizationReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.generatePdfReport("COATING_UTILIZATION", data);
        String filename = "coating_utilization_report_" + range.startDate().toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // --- Production Efficiency Reports ---

    @GetMapping("/efficiency")
    @Operation(summary = "Get production efficiency by order type report")
    @LoggableAction(value = "Retrieve Production Efficiency Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<ProductionEfficiencyReportDTO>> getEfficiencyReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(productionReportService.getProductionEfficiencyReport(
                range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/efficiency/export-excel")
    @Operation(summary = "Export production efficiency report to Excel")
    @LoggableAction(value = "Export Production Efficiency Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportEfficiencyExcel(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProductionEfficiencyReportDTO data = productionReportService.getProductionEfficiencyReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.exportEfficiencyReportToExcel(data);
        String filename = "production_efficiency_report_" + range.startDate().toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/efficiency/export-pdf")
    @Operation(summary = "Export production efficiency report to PDF")
    @LoggableAction(value = "Export Production Efficiency Report to PDF", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportEfficiencyPdf(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProductionEfficiencyReportDTO data = productionReportService.getProductionEfficiencyReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.generatePdfReport("PRODUCTION_EFFICIENCY", data);
        String filename = "production_efficiency_report_" + range.startDate().toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // --- Profit & Loss Reports ---

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Get profit and loss (P&L) report")
    @LoggableAction(value = "Retrieve Profit and Loss Report", action = AuditAction.GET_REPORT, entityType = "REPORT")
    public ResponseEntity<ApiResponse<ProfitLossReportDTO>> getProfitLossReport(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        return ResponseEntity.ok(ApiResponse.success(productionReportService.getProfitLossReport(
                range.startDate(), range.endDate(), label)));
    }

    @GetMapping("/profit-loss/export-excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Export profit and loss report to Excel")
    @LoggableAction(value = "Export Profit and Loss Report to Excel", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportProfitLossExcel(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProfitLossReportDTO data = productionReportService.getProfitLossReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.exportProfitLossReportToExcel(data);
        String filename = "profit_loss_report_" + range.startDate().toString() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/profit-loss/export-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Export profit and loss report to PDF")
    @LoggableAction(value = "Export Profit and Loss Report to PDF", action = AuditAction.DATA_EXPORTED, entityType = "REPORT")
    public ResponseEntity<byte[]> exportProfitLossPdf(
            @RequestParam(required = false) DateRangePreset preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        
        DateRangeResolver.DateRange range = resolveRange(preset, startDate, endDate);
        String label = preset != null ? preset.name() : range.startDate() + " to " + range.endDate();
        ProfitLossReportDTO data = productionReportService.getProfitLossReport(
                range.startDate(), range.endDate(), label);
        
        byte[] bytes = productionReportService.generatePdfReport("PROFIT_LOSS", data);
        String filename = "profit_loss_report_" + range.startDate().toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
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
