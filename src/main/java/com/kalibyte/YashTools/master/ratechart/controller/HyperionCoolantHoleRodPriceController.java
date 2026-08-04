package com.kalibyte.YashTools.master.ratechart.controller;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionCoolantHoleRodPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionCoolantHoleRodPriceResponse;
import com.kalibyte.YashTools.master.ratechart.service.HyperionCoolantHoleRodPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "Coolant Hole Rod Price", description = "APIs for managing coolant hole rod prices")
@RestController
@RequestMapping("/api/masters/rate-charts/coolant-hole-prices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HyperionCoolantHoleRodPriceController {

    private final HyperionCoolantHoleRodPriceService service;

    @Operation(
            summary = "Import rod net prices from Excel",
            description = "Upload an Excel file with columns: Item, K40UF/H10F, AM70/DM80, PN90, GP10/K10F"
    )
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Import Coolant Hole Prices Excel", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_IMPORTED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<String>> importExcel(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("=== Rod Net Price Import Request Started ===");
        log.info("File name: {}", file != null ? file.getOriginalFilename() : "null");
        log.info("File size: {} bytes", file != null ? file.getSize() : 0);
        log.info("Content type: {}", file != null ? file.getContentType() : "null");

        if (file == null || file.isEmpty()) {
            log.warn("Empty or null file received for import");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Please select a file to import"));
        }

        String filename = file.getOriginalFilename();

        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            log.warn("Invalid file format received: {}", filename);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Invalid file format. Please upload an Excel file (.xlsx or .xls)"));
        }

        try {
            log.info("Starting import process for file: {}", filename);
            int count = service.importExcel(file.getInputStream());
            log.info("=== Successfully imported {} rod net prices ===", count);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            count + " rate" + (count != 1 ? "s" : "") + " imported successfully",
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

    @Operation(summary = "Create new coolant hole price")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Create Coolant Hole Price Entry", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_CREATED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<HyperionCoolantHoleRodPriceResponse>> create(
            @Valid @RequestBody HyperionCoolantHoleRodPriceRequest request
    ) {
        log.info("Creating coolant hole price: category={}, item={}", request.getCategory(), request.getItem());
        HyperionCoolantHoleRodPriceResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rate chart entry created successfully", response));
    }

    @Operation(summary = "Update coolant hole price")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Update Coolant Hole Price Entry", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_UPDATED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<HyperionCoolantHoleRodPriceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HyperionCoolantHoleRodPriceRequest request
    ) {
        log.info("Updating coolant hole price with id: {}", id);
        HyperionCoolantHoleRodPriceResponse response = service.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Rate chart entry updated successfully", response)
        );
    }

    @Operation(summary = "Get coolant hole price by ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve Coolant Hole Price Entry", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<HyperionCoolantHoleRodPriceResponse>> getById(@PathVariable UUID id) {
        log.info("Fetching coolant hole price by id: {}", id);
        HyperionCoolantHoleRodPriceResponse response = service.getById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Rate chart entry retrieved successfully", response)
        );
    }

    @Operation(summary = "Get all active coolant hole prices")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve All Coolant Hole Prices", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<List<HyperionCoolantHoleRodPriceResponse>>> getAll() {
        log.info("Fetching all active coolant hole prices");
        List<HyperionCoolantHoleRodPriceResponse> responses = service.getAllActive();
        return ResponseEntity.ok(
                ApiResponse.success("Rates retrieved successfully", responses)
        );
    }

    @Operation(summary = "Get paginated active coolant hole prices")
    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    @LoggableAction(value = "Retrieve Paginated Coolant Hole Prices", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_VIEWED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<PageResponse<HyperionCoolantHoleRodPriceResponse>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("Fetching paginated coolant hole prices: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<HyperionCoolantHoleRodPriceResponse> pageResult = service.getAllActivePaginated(pageable);

        PageResponse<HyperionCoolantHoleRodPriceResponse> response = PageResponse.<HyperionCoolantHoleRodPriceResponse>builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Rates retrieved successfully", response));
    }

    @Operation(summary = "Deactivate coolant hole price")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @LoggableAction(value = "Deactivate Coolant Hole Price Entry", action = AuditAction.RATE_CHART_COOLANT_HOLE_ROD_DELETED, entityType = "RATE_CHART")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        log.info("Deactivating coolant hole price with id: {}", id);
        service.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Rate chart entry deactivated successfully", null));
    }
}