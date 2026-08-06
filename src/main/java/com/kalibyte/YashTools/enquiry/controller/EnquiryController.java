package com.kalibyte.YashTools.enquiry.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.enquiry.dto.request.CreateEnquiryRequest;
import com.kalibyte.YashTools.enquiry.dto.request.UpdateEnquiryStatusRequest;
import com.kalibyte.YashTools.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.YashTools.enquiry.service.EnquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
@Tag(name = "Enquiry Management", description = "APIs for managing customer enquiries")
@PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Operation(summary = "Create new enquiry")
    @LoggableAction(value = "Create Enquiry", action = AuditAction.ENQUIRY_CREATED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<EnquiryResponse>> createEnquiry(
            @Valid @RequestBody CreateEnquiryRequest request
    ) {
        EnquiryResponse response = enquiryService.createEnquiry(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enquiry created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES') or @enquirySecurityService.canAccessEnquiry(#id)")
    @Operation(summary = "Get enquiry by ID")
    @LoggableAction(value = "Retrieve Enquiry By ID", action = AuditAction.ENQUIRY_VIEWED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<EnquiryResponse>> getById(
            @Parameter(description = "Enquiry unique identifier")
            @PathVariable UUID id
    ) {
        EnquiryResponse response = enquiryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Enquiry retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Operation(summary = "Get all enquiries")
    @LoggableAction(value = "Retrieve All Enquiries", action = AuditAction.ENQUIRY_VIEWED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getAllEnquiries() {
        List<EnquiryResponse> responses = enquiryService.getAllEnquiries();
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved successfully", responses));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Operation(summary = "Get enquiries by customer")
    @LoggableAction(value = "Retrieve Enquiries By Customer", action = AuditAction.ENQUIRY_VIEWED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getEnquiriesByCustomer(
            @Parameter(description = "Customer unique identifier")
            @PathVariable UUID customerId
    ) {
        List<EnquiryResponse> responses = enquiryService.getEnquiriesByCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Found %d enquiries for customer", responses.size()),
                        responses
                )
        );
    }

    /**
     * Update enquiry status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Operation(
            summary = "Update enquiry status",
            description = "Updates the status of an enquiry with transition validation. " +
                    "Allowed transitions: CREATED→UNDER_REVIEW/QUOTED/CLOSED, " +
                    "UNDER_REVIEW→QUOTED/CLOSED, QUOTED→ACCEPTED/CLOSED, ACCEPTED→CLOSED"
    )
    @LoggableAction(value = "Update Enquiry Status", action = AuditAction.ENQUIRY_STATUS_UPDATED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<EnquiryResponse>> updateStatus(
            @Parameter(description = "Enquiry unique identifier")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnquiryStatusRequest request
    ) {
        log.info("Received status update request for enquiry: {} to status: {}", id, request.getStatus());

        EnquiryResponse response = enquiryService.updateEnquiryStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Enquiry status updated to %s successfully", response.getStatus()),
                        response
                )
        );
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Search enquiries")
    @LoggableAction(value = "Search Enquiries", action = AuditAction.ENQUIRY_VIEWED, entityType = "ENQUIRY")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> searchEnquiries(@RequestParam String query) {
        List<EnquiryResponse> result = enquiryService.searchEnquiries(query);
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved successfully", result));
    }

}