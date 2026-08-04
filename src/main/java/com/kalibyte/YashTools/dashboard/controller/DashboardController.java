package com.kalibyte.YashTools.dashboard.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.dashboard.dto.*;
import com.kalibyte.YashTools.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PRODUCTION', 'STORE', 'FINANCE')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        log.info("Fetching Admin Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard fetched successfully", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<SupervisorDashboardResponse>> getSupervisorDashboard() {
        log.info("Fetching Supervisor Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Supervisor dashboard fetched successfully", dashboardService.getSupervisorDashboard()));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<ManagerDashboardResponse>> getManagerDashboard() {
        log.info("Fetching Manager Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Manager dashboard fetched successfully", dashboardService.getManagerDashboard()));
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<SalesDashboardResponse>> getSalesDashboard() {
        log.info("Fetching Sales Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Sales dashboard fetched successfully", dashboardService.getSalesDashboard()));
    }

    @GetMapping("/production")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<ProductionDashboardResponse>> getProductionDashboard() {
        log.info("Fetching Production Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Production dashboard fetched successfully", dashboardService.getProductionDashboard()));
    }

    @GetMapping("/store")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<StoreDashboardResponse>> getStoreDashboard() {
        log.info("Fetching Store Dashboard metrics");
        return ResponseEntity.ok(ApiResponse.success("Store dashboard fetched successfully", dashboardService.getStoreDashboard()));
    }
}
