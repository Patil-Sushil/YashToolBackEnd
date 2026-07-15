package com.kalibyte.YashTools.inventory.transaction.materialissue.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueRequest;
import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.service.MaterialIssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/material-issues")
public class MaterialIssueController {

    private final MaterialIssueService service;

    public MaterialIssueController(MaterialIssueService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<MaterialIssueResponse>> issueMaterial(@Valid @RequestBody MaterialIssueRequest request) {
        MaterialIssueResponse response = service.issueMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material issued successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<MaterialIssueResponse>> getIssueById(@PathVariable UUID id) {
        MaterialIssueResponse response = service.getIssueById(id);
        return ResponseEntity.ok(ApiResponse.success("Material issue retrieved successfully", response));
    }

    @GetMapping("/number/{issueNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<MaterialIssueResponse>> getIssueByNumber(@PathVariable String issueNumber) {
        MaterialIssueResponse response = service.getIssueByNumber(issueNumber);
        return ResponseEntity.ok(ApiResponse.success("Material issue retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<MaterialIssueResponse>>> listIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MaterialIssueResponse> response = service.getAllIssues(page, size);
        return ResponseEntity.ok(ApiResponse.success("Material issues retrieved successfully", response));
    }
}
