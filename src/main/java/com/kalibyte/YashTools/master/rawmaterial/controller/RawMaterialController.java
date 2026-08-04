package com.kalibyte.YashTools.master.rawmaterial.controller;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;
import com.kalibyte.YashTools.master.rawmaterial.service.RawMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/masters/raw-materials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RawMaterialController {

    private final RawMaterialService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Create Raw Material", action = AuditAction.RAW_MATERIAL_CREATED, entityType = "RAW_MATERIAL")
    public ResponseEntity<ApiResponse<RawMaterialResponse>> create(
            @Valid @RequestBody RawMaterialRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Raw material created successfully",
                        service.create(request)
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Update Raw Material", action = AuditAction.RAW_MATERIAL_UPDATED, entityType = "RAW_MATERIAL")
    public ResponseEntity<ApiResponse<RawMaterialResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RawMaterialRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Raw material updated successfully",
                        service.update(id, request)
                )
        );
    }

    @GetMapping("/{id}")
    @LoggableAction(value = "Retrieve Raw Material By ID", action = AuditAction.RAW_MATERIAL_VIEWED, entityType = "RAW_MATERIAL")
    public ResponseEntity<ApiResponse<RawMaterialResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Raw material fetched successfully",
                        service.getById(id)
                )
        );
    }

    @GetMapping
    @LoggableAction(value = "Retrieve All Raw Materials", action = AuditAction.RAW_MATERIAL_VIEWED, entityType = "RAW_MATERIAL")
    public ResponseEntity<ApiResponse<List<RawMaterialResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Raw materials retrieved successfully",
                        service.getAllActive()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Deactivate Raw Material", action = AuditAction.RAW_MATERIAL_DELETED, entityType = "RAW_MATERIAL")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable UUID id
    ) {
        service.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Raw material deactivated successfully",
                        null
                )
        );
    }
}