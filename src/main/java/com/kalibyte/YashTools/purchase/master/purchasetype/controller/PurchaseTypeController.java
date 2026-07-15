package com.kalibyte.YashTools.purchase.master.purchasetype.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.request.PurchaseTypeRequest;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.response.PurchaseTypeResponse;
import com.kalibyte.YashTools.purchase.master.purchasetype.service.PurchaseTypeService;
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
@RequestMapping("/api/purchase/purchase-types")
@RequiredArgsConstructor
@Tag(name = "Purchase Type Master", description = "APIs for managing purchase types")
public class PurchaseTypeController {

    private final PurchaseTypeService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Create purchase type")
    public ResponseEntity<ApiResponse<PurchaseTypeResponse>> create(@Valid @RequestBody PurchaseTypeRequest request) {
        PurchaseTypeResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase type created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get purchase type by ID")
    public ResponseEntity<ApiResponse<PurchaseTypeResponse>> getById(@PathVariable UUID id) {
        PurchaseTypeResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase type retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Get all purchase types")
    public ResponseEntity<ApiResponse<List<PurchaseTypeResponse>>> getAll() {
        List<PurchaseTypeResponse> responses = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Purchase types list retrieved successfully", responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Update purchase type")
    public ResponseEntity<ApiResponse<PurchaseTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseTypeRequest request
    ) {
        PurchaseTypeResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase type updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE')")
    @Operation(summary = "Delete purchase type")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase type deleted successfully", null));
    }
}
