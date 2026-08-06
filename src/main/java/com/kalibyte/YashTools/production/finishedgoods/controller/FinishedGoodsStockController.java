package com.kalibyte.YashTools.production.finishedgoods.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.finishedgoods.dto.FinishedGoodsStockResponse;
import com.kalibyte.YashTools.production.finishedgoods.service.FinishedGoodsStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/finished-goods", "/api/production/finished-goods"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'QUALITY', 'INVENTORY', 'FINANCE')")
public class FinishedGoodsStockController {

    private final FinishedGoodsStockService service;

    @GetMapping({"/by-item/{workOrderItemId}", "/work-order-item/{workOrderItemId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'QUALITY', 'INVENTORY', 'FINANCE')")
    public ResponseEntity<ApiResponse<FinishedGoodsStockResponse>> getStockByItem(@PathVariable UUID workOrderItemId) {
        return ResponseEntity.ok(ApiResponse.success("Finished goods stock retrieved successfully", service.getStockByWorkOrderItem(workOrderItemId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION', 'QUALITY', 'INVENTORY', 'FINANCE')")
    public ResponseEntity<ApiResponse<PageResponse<FinishedGoodsStockResponse>>> listStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Finished goods stocks list retrieved successfully", service.getAllFinishedGoodsStock(page, size)));
    }
}
