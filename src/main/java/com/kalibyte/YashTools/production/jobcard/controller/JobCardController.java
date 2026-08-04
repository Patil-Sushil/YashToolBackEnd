package com.kalibyte.YashTools.production.jobcard.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.jobcard.dto.CreateJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.dto.JobCardResponse;
import com.kalibyte.YashTools.production.jobcard.dto.SplitJobCardRequest;
import com.kalibyte.YashTools.production.jobcard.service.JobCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-cards")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION')")
public class JobCardController {

    private final JobCardService jobCardService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobCardResponse>> create(@Valid @RequestBody CreateJobCardRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job Card created successfully", jobCardService.create(request)));
    }

    @PostMapping("/split")
    public ResponseEntity<ApiResponse<List<JobCardResponse>>> split(@Valid @RequestBody SplitJobCardRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job Card split successfully", jobCardService.split(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobCardResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(jobCardService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobCardResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<JobCardResponse> result = jobCardService.list(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<ApiResponse<JobCardResponse>> updatePriority(
            @PathVariable UUID id,
            @RequestParam Integer priority) {
        return ResponseEntity.ok(ApiResponse.success("Job Card priority updated", jobCardService.updatePriority(id, priority)));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<JobCardResponse>> hold(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("Job Card put on hold", jobCardService.hold(id, reason)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<JobCardResponse>> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("Job Card cancelled", jobCardService.cancel(id, reason)));
    }
}
