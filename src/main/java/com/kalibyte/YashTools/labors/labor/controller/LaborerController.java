package com.kalibyte.YashTools.labors.labor.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.YashTools.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.YashTools.labors.labor.entity.Enum.LaborRole;
import com.kalibyte.YashTools.labors.labor.service.LaborerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labors")
@RequiredArgsConstructor
@Tag(name = "Laborer Management", description = "APIs for managing laborers and their profiles")
@SecurityRequirement(name = "bearerAuth")
public class LaborerController {

    private final LaborerService laborerService;

    @PostMapping
    @Operation(summary = "Create a new laborer", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Register Laborer", action = AuditAction.LABORER_CREATED, entityType = "LABORER")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> createLaborer(@Valid @RequestBody LaborerRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Laborer created successfully", laborerService.createLaborer(request)));
    }

    @GetMapping
    @Operation(summary = "Get all laborers", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Retrieve All Laborers", action = AuditAction.LABORER_VIEWED, entityType = "LABORER")
    public ResponseEntity<ApiResponse<List<LaborerResponseDTO>>> getAllLaborers(@RequestParam(required = false) LaborRole role) {
        if (role != null) {
            return ResponseEntity.ok(ApiResponse.success(laborerService.getLaborersByRole(role)));
        }
        return ResponseEntity.ok(ApiResponse.success(laborerService.getAllLaborers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get laborer by ID", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Retrieve Laborer By ID", action = AuditAction.LABORER_VIEWED, entityType = "LABORER")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> getLaborerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(laborerService.getLaborerById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update laborer by ID", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Update Laborer Info", action = AuditAction.LABORER_UPDATED, entityType = "LABORER")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> updateLaborer(@PathVariable Long id, @Valid @RequestBody LaborerRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Laborer updated successfully", laborerService.updateLaborer(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a labor", description = "Only accessible by ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @LoggableAction(value = "Deactivate Laborer", action = AuditAction.LABORER_UPDATED, entityType = "LABORER")
    public ResponseEntity<ApiResponse<LaborerResponseDTO>> deactivateLaborer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Deactivated the labor",laborerService.deleteLaborer(id)));
    }

}
