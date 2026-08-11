package com.kalibyte.YashTools.production.logistics.damage.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.util.SecurityUtils;
import com.kalibyte.YashTools.production.logistics.damage.dto.CreateTransitDamageRequest;
import com.kalibyte.YashTools.production.logistics.damage.dto.TransitDamageResponse;
import com.kalibyte.YashTools.production.logistics.damage.service.TransitDamageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transit-damages")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'QUALITY', 'PRODUCTION', 'DELIVERY', 'SALES')")
public class TransitDamageController {

    private final TransitDamageService transitDamageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'QUALITY', 'PRODUCTION', 'DELIVERY')")
    public ResponseEntity<ApiResponse<TransitDamageResponse>> reportTransitDamage(
            @Valid @RequestBody CreateTransitDamageRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("Reporting transit damage for Challan: {}, Item: {} by {}", 
                request.getDeliveryChallanId(), request.getWorkOrderItemId(), username);
        TransitDamageResponse response = transitDamageService.reportTransitDamage(request, username);
        return ResponseEntity.ok(ApiResponse.success("Transit damage reported successfully", response));
    }

    @PostMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION', 'DELIVERY', 'STORE')")
    public ResponseEntity<ApiResponse<TransitDamageResponse>> approveReport(@PathVariable UUID id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("Approving transit damage report: {} by {}", id, username);
        TransitDamageResponse response = transitDamageService.approveReport(id, username);
        return ResponseEntity.ok(ApiResponse.success("Transit damage report approved and processed successfully", response));
    }

    @PostMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION', 'DELIVERY', 'STORE')")
    public ResponseEntity<ApiResponse<TransitDamageResponse>> rejectReport(@PathVariable UUID id) {
        String username = SecurityUtils.getCurrentUsername();
        log.info("Rejecting transit damage report: {} by {}", id, username);
        TransitDamageResponse response = transitDamageService.rejectReport(id, username);
        return ResponseEntity.ok(ApiResponse.success("Transit damage report rejected successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'QUALITY', 'PRODUCTION', 'DELIVERY', 'SALES')")
    public ResponseEntity<ApiResponse<List<TransitDamageResponse>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.success("Transit damage reports retrieved successfully", transitDamageService.getAllReports()));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'QUALITY', 'PRODUCTION', 'DELIVERY', 'SALES')")
    public ResponseEntity<ApiResponse<TransitDamageResponse>> getReportById(@PathVariable UUID id) {
        TransitDamageResponse response = transitDamageService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/challan/{challanId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'QUALITY', 'PRODUCTION', 'SALES')")
    public ResponseEntity<ApiResponse<List<TransitDamageResponse>>> getDamageReportsForChallan(
            @PathVariable UUID challanId) {
        List<TransitDamageResponse> response = transitDamageService.getDamageReportsForChallan(challanId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
