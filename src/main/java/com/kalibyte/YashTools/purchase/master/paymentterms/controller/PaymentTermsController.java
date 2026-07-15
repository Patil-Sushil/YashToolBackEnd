package com.kalibyte.YashTools.purchase.master.paymentterms.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.request.PaymentTermsRequest;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.response.PaymentTermsResponse;
import com.kalibyte.YashTools.purchase.master.paymentterms.service.PaymentTermsService;
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
@RequestMapping("/api/purchase/payment-terms")
@RequiredArgsConstructor
@Tag(name = "Payment Terms Master", description = "APIs for managing payment terms")
public class PaymentTermsController {

    private final PaymentTermsService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Create payment terms")
    public ResponseEntity<ApiResponse<PaymentTermsResponse>> create(@Valid @RequestBody PaymentTermsRequest request) {
        PaymentTermsResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment terms created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get payment terms by ID")
    public ResponseEntity<ApiResponse<PaymentTermsResponse>> getById(@PathVariable UUID id) {
        PaymentTermsResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment terms retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all payment terms")
    public ResponseEntity<ApiResponse<List<PaymentTermsResponse>>> getAll() {
        List<PaymentTermsResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Payment terms list retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Update payment terms")
    public ResponseEntity<ApiResponse<PaymentTermsResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentTermsRequest request
    ) {
        PaymentTermsResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment terms updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Delete payment terms")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment terms deleted successfully", null));
    }
}
