package com.kalibyte.YashTools.inventory.transaction.stocktake.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeRequest;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.service.StockTakeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/stock-takes")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
public class StockTakeController {

    private final StockTakeService service;

    public StockTakeController(StockTakeService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<StockTakeResponse>> createStockTake(@Valid @RequestBody StockTakeRequest request) {
        StockTakeResponse response = service.createStockTake(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock take created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<StockTakeResponse>> getStockTakeById(@PathVariable UUID id) {
        StockTakeResponse response = service.getStockTakeById(id);
        return ResponseEntity.ok(ApiResponse.success("Stock take retrieved successfully", response));
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<StockTakeResponse>> getStockTakeByNumber(@PathVariable String number) {
        StockTakeResponse response = service.getStockTakeByNumber(number);
        return ResponseEntity.ok(ApiResponse.success("Stock take retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<PageResponse<StockTakeResponse>>> listStockTakes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<StockTakeResponse> response = service.getAllStockTakes(page, size);
        return ResponseEntity.ok(ApiResponse.success("Stock takes retrieved successfully", response));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<StockTakeResponse>> completeStockTake(@PathVariable UUID id) {
        StockTakeResponse response = service.completeStockTake(id);
        return ResponseEntity.ok(ApiResponse.success("Stock take completed successfully", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockTakeResponse>> approveStockTake(@PathVariable UUID id) {
        StockTakeResponse response = service.approveStockTake(id);
        return ResponseEntity.ok(ApiResponse.success("Stock take approved and differences reconciled", response));
    }
}
