package com.kalibyte.YashTools.inventory.transaction.stock.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.stock.dto.StockResponse;
import com.kalibyte.YashTools.inventory.transaction.stock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/stocks")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/query")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<StockResponse>> getStock(
            @RequestParam UUID itemId,
            @RequestParam(required = false) UUID materialGradeId) {
        StockResponse response = stockService.getStock(itemId, materialGradeId);
        return ResponseEntity.ok(ApiResponse.success("Stock retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<StockResponse>>> listStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<StockResponse> response = stockService.getAllStocks(page, size);
        return ResponseEntity.ok(ApiResponse.success("Stocks retrieved successfully", response));
    }
}
