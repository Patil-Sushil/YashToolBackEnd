package com.kalibyte.YashTools.workorder.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.workorder.dto.request.CreateWorkOrderRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderStatusRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateTrialResultRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderPlanningRequest;
import com.kalibyte.YashTools.workorder.dto.response.WorkOrderResponse;
import com.kalibyte.YashTools.workorder.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kalibyte.YashTools.workorder.dto.response.LockedQuotationSummaryResponse;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PRODUCTION', 'DELIVERY', 'STORE', 'QUALITY', 'FINANCE')")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping({"", "/from-quotation"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> createFromQuotation(
            @Valid @RequestBody CreateWorkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Work Order created successfully",
                workOrderService.createFromQuotation(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<WorkOrderResponse> result = workOrderService.list(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/locked-quotations/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<List<LockedQuotationSummaryResponse>>> getAllLockedQuotations() {
        return ResponseEntity.ok(ApiResponse.success(
                "All available locked quotations retrieved successfully",
                workOrderService.getAllLockedQuotationsAvailable()));
    }

    @GetMapping("/locked-quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<List<LockedQuotationSummaryResponse>>> getLockedQuotationsForCustomer(
            @RequestParam UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Locked quotations retrieved successfully",
                workOrderService.getLockedQuotationsForCustomer(customerId)));
    }

    @GetMapping("/by-number/{workOrderNo}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getByNumber(@PathVariable String workOrderNo) {
        return ResponseEntity.ok(ApiResponse.success(workOrderService.getByNumber(workOrderNo)));
    }

    @GetMapping("/pending-dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PRODUCTION', 'DELIVERY', 'STORE', 'QUALITY', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<com.kalibyte.YashTools.workorder.dto.response.PendingDispatchWorkOrderResponse>>> getPendingDispatchWorkOrders(
            @RequestParam(required = false) UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending dispatch work orders retrieved successfully",
                workOrderService.getPendingDispatchWorkOrders(customerId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderResponse>>> searchWorkOrders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<WorkOrderResponse> result = workOrderService.searchWorkOrders(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Work Orders retrieved successfully", PageResponse.from(result)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(workOrderService.getById(id)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/progress")
    public ResponseEntity<ApiResponse<com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse>> getProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Work Order progress retrieved successfully", workOrderService.getProgress(id)));
    }

    @PutMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/status")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Work Order status updated successfully",
                workOrderService.updateStatus(id, request)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/progress")
    public ResponseEntity<ApiResponse<com.kalibyte.YashTools.workorder.dto.response.WorkOrderProgressResponse>> getProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Work Order progress retrieved successfully", workOrderService.getProgress(id)));
    }

    @PutMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/planning")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> updatePlanning(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkOrderPlanningRequest request) {
        log.info("Updating planning dates for Work Order: {} by production planner", id);
        return ResponseEntity.ok(ApiResponse.success(
                "Work Order planning dates updated successfully",
                workOrderService.updatePlanning(id, request)));
    }

}
