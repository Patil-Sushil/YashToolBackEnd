package com.example.YashToolBackEnd.master.rawmaterial.controller;

import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialRequest;
import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialResponse;
import com.example.YashToolBackEnd.master.rawmaterial.service.RawMaterialService;
import com.example.YashToolBackEnd.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masters/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController {

    private final RawMaterialService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ResponseEntity<ApiResponse<RawMaterialResponse>> create(@Valid @RequestBody RawMaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Raw material created successfully", service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RawMaterialResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Raw materials retrieved successfully", service.getAllActive()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Raw material deactivated successfully", null));
    }
}
