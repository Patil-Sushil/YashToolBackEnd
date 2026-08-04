package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.service.PurchaseInvoiceService;
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
@RequestMapping("/api/purchase/purchase-invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoice", description = "APIs for managing Purchase Invoices")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Record purchase invoice")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> create(@Valid @RequestBody PurchaseInvoiceRequest request) {
        PurchaseInvoiceResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase Invoice recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get purchase invoice by ID")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> getById(@PathVariable UUID id) {
        PurchaseInvoiceResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase Invoice retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all purchase invoices")
    public ResponseEntity<ApiResponse<List<PurchaseInvoiceResponse>>> getAll() {
        List<PurchaseInvoiceResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Purchase Invoices list retrieved successfully", responses));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Update purchase invoice status")
    public ResponseEntity<ApiResponse<PurchaseInvoiceResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.UpdatePurchaseInvoiceStatusRequest request
    ) {
        PurchaseInvoiceResponse response = service.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase Invoice status updated successfully", response));
    }
}
