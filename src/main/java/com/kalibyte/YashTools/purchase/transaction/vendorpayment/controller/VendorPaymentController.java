package com.kalibyte.YashTools.purchase.transaction.vendorpayment.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.service.VendorPaymentService;
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
@RequestMapping("/api/purchase/vendor-payments")
@RequiredArgsConstructor
@Tag(name = "Vendor Payment", description = "APIs for managing Vendor Payments")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
public class VendorPaymentController {

    private final VendorPaymentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Record vendor payment")
    public ResponseEntity<ApiResponse<VendorPaymentResponse>> create(@Valid @RequestBody VendorPaymentRequest request) {
        VendorPaymentResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor payment recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get vendor payment by ID")
    public ResponseEntity<ApiResponse<VendorPaymentResponse>> getById(@PathVariable UUID id) {
        VendorPaymentResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all vendor payments")
    public ResponseEntity<ApiResponse<List<VendorPaymentResponse>>> getAll() {
        List<VendorPaymentResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Vendor payments list retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Update vendor payment")
    public ResponseEntity<ApiResponse<VendorPaymentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VendorPaymentRequest request
    ) {
        VendorPaymentResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Cancel/delete vendor payment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment cancelled and deleted successfully", null));
    }

    // Reports Endpoints

    @GetMapping("/reports/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get vendor outstanding invoices report")
    public ResponseEntity<ApiResponse<List<PurchaseInvoiceResponse>>> getOutstandingReport(
            @RequestParam(required = false) UUID vendorId
    ) {
        List<PurchaseInvoiceResponse> responses = service.getOutstandingReport(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor outstanding report retrieved successfully", responses));
    }

    @GetMapping("/reports/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get vendor payment history report")
    public ResponseEntity<ApiResponse<List<VendorPaymentResponse>>> getPaymentHistoryReport(
            @RequestParam(required = false) UUID vendorId
    ) {
        List<VendorPaymentResponse> responses = service.getPaymentHistoryReport(vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment history report retrieved successfully", responses));
    }

    @GetMapping("/reports/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get overdue invoices report")
    public ResponseEntity<ApiResponse<List<PurchaseInvoiceResponse>>> getOverdueInvoices() {
        List<PurchaseInvoiceResponse> responses = service.getOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success("Overdue invoices report retrieved successfully", responses));
    }
}
