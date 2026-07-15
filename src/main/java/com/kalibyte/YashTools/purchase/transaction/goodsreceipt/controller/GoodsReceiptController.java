package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.service.GoodsReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase/goods-receipts")
@RequiredArgsConstructor
@Tag(name = "Goods Receipt Note (GRN)", description = "APIs for managing Goods Receipt Notes")
public class GoodsReceiptController {

    private final GoodsReceiptService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @Operation(summary = "Create goods receipt note (GRN)")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> create(@Valid @RequestBody GoodsReceiptRequest request) {
        GoodsReceiptResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goods Receipt Note created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get goods receipt note by ID")
    public ResponseEntity<ApiResponse<GoodsReceiptResponse>> getById(@PathVariable UUID id) {
        GoodsReceiptResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Goods Receipt Note retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all goods receipt notes")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponse>>> getAll() {
        List<GoodsReceiptResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Goods Receipt Notes list retrieved successfully", responses));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get goods receipt notes by purchase order ID")
    public ResponseEntity<ApiResponse<List<GoodsReceiptResponse>>> getByPurchaseOrderId(@PathVariable UUID purchaseOrderId) {
        List<GoodsReceiptResponse> responses = service.getByPurchaseOrderId(purchaseOrderId);
        return ResponseEntity.ok(ApiResponse.success("Goods Receipt Notes retrieved successfully", responses));
    }
}
