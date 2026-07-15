package com.kalibyte.YashTools.purchase.transaction.purchasereturn.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request.PurchaseReturnRequest;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response.PurchaseReturnResponse;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.service.PurchaseReturnService;
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
@RequestMapping("/api/purchase/purchase-returns")
@RequiredArgsConstructor
@Tag(name = "Purchase Return", description = "APIs for managing Purchase Returns")
public class PurchaseReturnController {

    private final PurchaseReturnService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    @Operation(summary = "Record purchase return")
    public ResponseEntity<ApiResponse<PurchaseReturnResponse>> create(@Valid @RequestBody PurchaseReturnRequest request) {
        PurchaseReturnResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase Return recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get purchase return by ID")
    public ResponseEntity<ApiResponse<PurchaseReturnResponse>> getById(@PathVariable UUID id) {
        PurchaseReturnResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Return retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all purchase returns")
    public ResponseEntity<ApiResponse<List<PurchaseReturnResponse>>> getAll() {
        List<PurchaseReturnResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Purchase Returns list retrieved successfully", responses));
    }
}
