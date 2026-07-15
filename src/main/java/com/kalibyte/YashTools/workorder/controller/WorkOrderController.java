package com.kalibyte.YashTools.workorder.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.workorder.dto.request.CreateWorkOrderRequest;
import com.kalibyte.YashTools.workorder.dto.request.UpdateWorkOrderStatusRequest;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkOrderResponse>> createFromQuotation(
            @Valid @RequestBody CreateWorkOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Work Order created successfully",
                workOrderService.createFromQuotation(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(workOrderService.getById(id)));
    }

    @GetMapping("/by-number/{workOrderNo}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getByNumber(@PathVariable String workOrderNo) {
        return ResponseEntity.ok(ApiResponse.success(workOrderService.getByNumber(workOrderNo)));
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

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Work Order status updated successfully",
                workOrderService.updateStatus(id, request)));
    }
}
