package com.kalibyte.YashTools.production.execution.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.production.execution.dto.ExecutionResponse;
import com.kalibyte.YashTools.production.execution.dto.StartJobRequest;
import com.kalibyte.YashTools.production.execution.dto.UpdateProgressRequest;
import com.kalibyte.YashTools.production.execution.service.ExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production-executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ExecutionResponse>> startJob(@Valid @RequestBody StartJobRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job session started successfully", executionService.startJob(request)));
    }

    @PutMapping("/{logId}/progress")
    public ResponseEntity<ApiResponse<ExecutionResponse>> updateProgress(
            @PathVariable UUID logId,
            @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", executionService.updateProgress(logId, request)));
    }

    @GetMapping("/active/by-job-card/{jobCardId}")
    public ResponseEntity<ApiResponse<ExecutionResponse>> getActiveLogByJobCard(@PathVariable UUID jobCardId) {
        return ResponseEntity.ok(ApiResponse.success(executionService.getActiveLogByJobCard(jobCardId)));
    }

    @GetMapping("/active/by-operator/{operatorId}")
    public ResponseEntity<ApiResponse<ExecutionResponse>> getActiveLogByOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(ApiResponse.success(executionService.getActiveLogByOperator(operatorId)));
    }

    @GetMapping("/by-job-card/{jobCardId}")
    public ResponseEntity<ApiResponse<List<ExecutionResponse>>> getLogsByJobCard(@PathVariable UUID jobCardId) {
        return ResponseEntity.ok(ApiResponse.success(executionService.getLogsByJobCard(jobCardId)));
    }
}
