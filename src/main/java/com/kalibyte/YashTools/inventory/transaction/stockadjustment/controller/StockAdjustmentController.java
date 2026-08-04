package com.kalibyte.YashTools.inventory.transaction.stockadjustment.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentRequest;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentResponse;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.service.StockAdjustmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/stock-adjustments")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
public class StockAdjustmentController {

    private final StockAdjustmentService service;

    public StockAdjustmentController(StockAdjustmentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        StockAdjustmentResponse response = service.adjustStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock adjusted successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> getAdjustmentById(@PathVariable UUID id) {
        StockAdjustmentResponse response = service.getAdjustmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment retrieved successfully", response));
    }

    @GetMapping("/number/{adjustmentNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> getAdjustmentByNumber(@PathVariable String adjustmentNumber) {
        StockAdjustmentResponse response = service.getAdjustmentByNumber(adjustmentNumber);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<PageResponse<StockAdjustmentResponse>>> listAdjustments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<StockAdjustmentResponse> response = service.getAllAdjustments(page, size);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustments retrieved successfully", response));
    }
}
