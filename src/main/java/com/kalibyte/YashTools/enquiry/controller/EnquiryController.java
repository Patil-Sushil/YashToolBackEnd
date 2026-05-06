package com.kalibyte.YashTools.enquiry.controller;

import com.kalibyte.YashTools.enquiry.dto.request.CreateEnquiryRequest;
import com.kalibyte.YashTools.enquiry.dto.response.EnquiryResponse;
import com.kalibyte.YashTools.enquiry.service.EnquiryService;
import com.kalibyte.YashTools.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {
    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }
    // Create  new Enquiry
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<EnquiryResponse>> createEnquiry(@Valid @RequestBody CreateEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enquiry created successfully",
                        enquiryService.createEnquiry(request)));
    }

    // Get enquiry by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES') or @securityService.canAccessEnquiry(#id)")
    public ResponseEntity<ApiResponse<EnquiryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Enquiry retrieved successfully", enquiryService.getById(id)));
    }

    // Getting All Enquiries
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getAllEnquiries() {
        return ResponseEntity.ok(ApiResponse.success("Enquiries retrieved successfully", enquiryService.getAllEnquiries()));
    }

}
