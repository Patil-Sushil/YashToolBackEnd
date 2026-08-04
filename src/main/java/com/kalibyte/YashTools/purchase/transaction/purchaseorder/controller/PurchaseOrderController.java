package com.kalibyte.YashTools.purchase.transaction.purchaseorder.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.UpdatePurchaseOrderStatusRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.service.PurchaseOrderService;
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
@RequestMapping("/api/purchase/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order", description = "APIs for managing purchase orders")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Create purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> create(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getById(@PathVariable UUID id) {
        PurchaseOrderResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all purchase orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getAll() {
        List<PurchaseOrderResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Purchase orders list retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Update purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderRequest request
    ) {
        PurchaseOrderResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Update purchase order status")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderStatusRequest request
    ) {
        PurchaseOrderResponse response = service.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase order status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Delete purchase order")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order deleted successfully", null));
    }
}
