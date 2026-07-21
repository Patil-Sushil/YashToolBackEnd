package com.kalibyte.YashTools.production.tracking.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.production.tracking.dto.PlannerDashboardResponse;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionRequest;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionResponse;
import com.kalibyte.YashTools.production.tracking.service.QualityInspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quality-inspections")
@RequiredArgsConstructor
public class QualityInspectionController {

    private final QualityInspectionService qualityInspectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> inspect(
            @Valid @RequestBody QualityInspectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("QC Inspection recorded successfully", qualityInspectionService.inspect(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(qualityInspectionService.getById(id)));
    }

    @GetMapping("/by-job-card/{jobCardId}")
    public ResponseEntity<ApiResponse<QualityInspectionResponse>> getByJobCardId(@PathVariable UUID jobCardId) {
        return ResponseEntity.ok(ApiResponse.success(qualityInspectionService.getByJobCardId(jobCardId)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PlannerDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(qualityInspectionService.getDashboard()));
    }
}
