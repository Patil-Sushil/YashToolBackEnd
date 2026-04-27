package com.example.YashToolBackEnd.master.coating.controller;

import com.example.YashToolBackEnd.master.coating.dto.CoatingRequest;
import com.example.YashToolBackEnd.master.coating.dto.CoatingResponse;
import com.example.YashToolBackEnd.master.coating.service.CoatingService;
import com.example.YashToolBackEnd.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masters/coatings")
@RequiredArgsConstructor
public class CoatingController {
    private final CoatingService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ResponseEntity<ApiResponse<CoatingResponse>> create(@Valid @RequestBody CoatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Coating created successfully", service.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CoatingResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Coatings retrieved successfully", service.getAllActive()));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Coating deactivated successfully", null));
    }
}
