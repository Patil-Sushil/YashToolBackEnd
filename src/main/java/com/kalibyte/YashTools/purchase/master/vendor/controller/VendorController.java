package com.kalibyte.YashTools.purchase.master.vendor.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.master.vendor.dto.request.VendorRequest;
import com.kalibyte.YashTools.purchase.master.vendor.dto.response.VendorResponse;
import com.kalibyte.YashTools.purchase.master.vendor.service.VendorService;
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
@RequestMapping("/api/purchase/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Master", description = "APIs for managing vendors")
public class VendorController {

    private final VendorService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Create vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> create(@Valid @RequestBody VendorRequest request) {
        VendorResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getById(@PathVariable UUID id) {
        VendorResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all vendors")
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getAll() {
        List<VendorResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Vendors list retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Update vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VendorRequest request
    ) {
        VendorResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Delete vendor")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully", null));
    }

    @GetMapping("/{id}/outstanding-balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get vendor remaining outstanding balance")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getOutstandingBalance(@PathVariable UUID id) {
        java.math.BigDecimal balance = service.getOutstandingBalance(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor outstanding balance retrieved successfully", balance));
    }
}
