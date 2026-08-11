package com.kalibyte.YashTools.production.packing.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.packing.dto.PackingRequest;
import com.kalibyte.YashTools.production.packing.dto.PackingResponse;
import com.kalibyte.YashTools.production.packing.service.PackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/packing", "/api/production/packing"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION', 'DELIVERY')")
public class PackingController {

    private final PackingService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'DELIVERY')")
    public ResponseEntity<ApiResponse<PackingResponse>> recordPacking(@Valid @RequestBody PackingRequest request) {
        PackingResponse response = service.recordPacking(request);
        return ResponseEntity.ok(ApiResponse.success("Packing log recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'DELIVERY')")
    public ResponseEntity<ApiResponse<PackingResponse>> getPackingById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getPackingById(id)));
    }

    @GetMapping({"/by-item/{workOrderItemId}", "/work-order-item/{workOrderItemId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'DELIVERY')")
    public ResponseEntity<ApiResponse<List<PackingResponse>>> getPackingByItem(@PathVariable UUID workOrderItemId) {
        return ResponseEntity.ok(ApiResponse.success(service.getPackingByWorkOrderItem(workOrderItemId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'DELIVERY')")
    public ResponseEntity<ApiResponse<PageResponse<PackingResponse>>> listPackingLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Packing logs retrieved successfully", service.getAllPackingLogs(page, size)));
    }
}
