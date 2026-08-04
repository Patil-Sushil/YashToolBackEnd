package com.kalibyte.YashTools.master.ratechart.controller;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.master.ratechart.dto.request.ToolServiceRateMasterRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.ToolServiceRateMasterResponse;
import com.kalibyte.YashTools.master.ratechart.service.ToolServiceRateMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Tool Service Rate Master", description = "APIs for managing service rates (Re-Sharpening, Re-Forming, Coating Only)")
@RestController
@RequestMapping("/api/masters/rate-charts/tool-service-rates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ToolServiceRateMasterController {

    private final ToolServiceRateMasterService service;

    @Operation(
            summary = "Import tool service rates from Excel",
            description = "Upload an Excel file with columns: Service, Tool, Dia From, Dia To, Material, Base Rate, Minor, Medium, Major, TiN, TiAlN, AlCrN, DLC, Special Geometry, Special Profile, Express, Days"
    )
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Import Tool Service Rates Excel", action = AuditAction.TOOL_SERVICE_RATE_MASTER_IMPORTED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<String>> importExcel(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Tool Service Rate Master import request received");

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Please select a file to import"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Invalid file format. Please upload an Excel file (.xlsx or .xls)"));
        }

        try {
            int count = service.importExcel(file.getInputStream());
            return ResponseEntity.ok(
                    ApiResponse.success(
                            count + " tool service rate" + (count != 1 ? "s" : "") + " imported successfully",
                            String.valueOf(count)
                    )
            );
        } catch (IOException e) {
            log.error("Failed to read file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Failed to read file: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid data in file: {}", filename, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during import of {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Import failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Create new tool service rate")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Create Tool Service Rate", action = AuditAction.TOOL_SERVICE_RATE_MASTER_CREATED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<ToolServiceRateMasterResponse>> create(
            @Valid @RequestBody ToolServiceRateMasterRequest request
    ) {
        log.info("Creating service rate: serviceType={}, toolType={}", request.getServiceType(), request.getToolType());
        ToolServiceRateMasterResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service rate entry created successfully", response));
    }

    @Operation(summary = "Update tool service rate")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Update Tool Service Rate", action = AuditAction.TOOL_SERVICE_RATE_MASTER_UPDATED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<ToolServiceRateMasterResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ToolServiceRateMasterRequest request
    ) {
        log.info("Updating service rate with id: {}", id);
        ToolServiceRateMasterResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service rate entry updated successfully", response));
    }

    @Operation(summary = "Get tool service rate by ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve Tool Service Rate", action = AuditAction.TOOL_SERVICE_RATE_MASTER_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<ToolServiceRateMasterResponse>> getById(@PathVariable UUID id) {
        ToolServiceRateMasterResponse response = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Service rate entry retrieved successfully", response));
    }

    @Operation(summary = "Get all active tool service rates")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve All Active Tool Service Rates", action = AuditAction.TOOL_SERVICE_RATE_MASTER_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<List<ToolServiceRateMasterResponse>>> getAll() {
        List<ToolServiceRateMasterResponse> responses = service.getAllActive();
        return ResponseEntity.ok(ApiResponse.success("Service rates retrieved successfully", responses));
    }

    @Operation(summary = "Get paginated active tool service rates")
    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve Paginated Active Tool Service Rates", action = AuditAction.TOOL_SERVICE_RATE_MASTER_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<PageResponse<ToolServiceRateMasterResponse>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ToolServiceRateMasterResponse> pageResult = service.getAllActivePaginated(pageable);

        PageResponse<ToolServiceRateMasterResponse> response = PageResponse.<ToolServiceRateMasterResponse>builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Service rates retrieved successfully", response));
    }

    @Operation(summary = "Deactivate tool service rate")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Deactivate Tool Service Rate", action = AuditAction.TOOL_SERVICE_RATE_MASTER_DELETED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Service rate entry deactivated successfully", null));
    }
}
