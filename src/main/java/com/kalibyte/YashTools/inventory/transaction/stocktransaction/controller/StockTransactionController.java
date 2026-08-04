package com.kalibyte.YashTools.inventory.transaction.stocktransaction.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.dto.StockTransactionResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.service.StockTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/stock-transactions")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
public class StockTransactionController {

    private final StockTransactionService service;

    public StockTransactionController(StockTransactionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> getTransactionById(@PathVariable UUID id) {
        StockTransactionResponse response = service.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success("Stock transaction retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionResponse>>> listTransactions(
            @RequestParam(required = false) StockTransactionType type,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) String referenceNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<StockTransactionResponse> response = service.searchTransactions(type, itemId, referenceNumber, page, size);
        return ResponseEntity.ok(ApiResponse.success("Stock transactions retrieved successfully", response));
    }
}
