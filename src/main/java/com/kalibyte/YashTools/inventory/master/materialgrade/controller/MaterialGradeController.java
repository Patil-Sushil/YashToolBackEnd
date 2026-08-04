package com.kalibyte.YashTools.inventory.master.materialgrade.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeRequest;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.service.MaterialGradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/material-grades")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
public class MaterialGradeController {

    private final MaterialGradeService materialGradeService;

    public MaterialGradeController(MaterialGradeService materialGradeService) {
        this.materialGradeService = materialGradeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<MaterialGradeResponse>> createMaterialGrade(@Valid @RequestBody MaterialGradeRequest request) {
        MaterialGradeResponse response = materialGradeService.createMaterialGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material grade created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")
    public ResponseEntity<ApiResponse<MaterialGradeResponse>> updateMaterialGrade(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialGradeRequest request) {
        MaterialGradeResponse response = materialGradeService.updateMaterialGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Material grade updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<MaterialGradeResponse>> getMaterialGradeById(@PathVariable UUID id) {
        MaterialGradeResponse response = materialGradeService.getMaterialGradeById(id);
        return ResponseEntity.ok(ApiResponse.success("Material grade retrieved successfully", response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<MaterialGradeResponse>>> getAllMaterialGrades() {
        List<MaterialGradeResponse> response = materialGradeService.getAllMaterialGrades();
        return ResponseEntity.ok(ApiResponse.success("All material grades retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'FINANCE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<MaterialGradeResponse>>> listMaterialGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MaterialGradeResponse> response = materialGradeService.getAllMaterialGrades(page, size);
        return ResponseEntity.ok(ApiResponse.success("Material grades retrieved successfully", response));
    }
}
