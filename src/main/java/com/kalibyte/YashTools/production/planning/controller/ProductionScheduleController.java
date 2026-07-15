package com.kalibyte.YashTools.production.planning.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.planning.dto.ScheduleRequest;
import com.kalibyte.YashTools.production.planning.dto.ScheduleResponse;
import com.kalibyte.YashTools.production.planning.service.ProductionScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production-schedules")
@RequiredArgsConstructor
public class ProductionScheduleController {

    private final ProductionScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> schedule(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job scheduled successfully", scheduleService.schedule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> reschedule(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job rescheduled successfully", scheduleService.reschedule(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getById(id)));
    }

    @GetMapping("/by-job-card/{jobCardId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getByJobCardId(@PathVariable UUID jobCardId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getByJobCardId(jobCardId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ScheduleResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ScheduleResponse> result = scheduleService.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }
}
