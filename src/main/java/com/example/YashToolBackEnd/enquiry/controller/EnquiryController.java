package com.example.YashToolBackEnd.enquiry.controller;

import com.example.YashToolBackEnd.enquiry.dto.request.CreateEnquiryRequest;
import com.example.YashToolBackEnd.enquiry.dto.response.EnquiryResponse;
import com.example.YashToolBackEnd.enquiry.service.EnquiryService;
import com.example.YashToolBackEnd.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {
    private final EnquiryService enquiryService;

    // Create  new Enquiry
    @PostMapping
    public ResponseEntity<ApiResponse<EnquiryResponse>> createEmquiry(@Valid @RequestBody CreateEnquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Enquiry created successfully", enquiryService.createEnquiry(request)));

    }

    // Get enquiry by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnquiryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Enquiry retrieved successfully", enquiryService.getById(id)));
    }

    // Getting All Enquiries
    @GetMapping
    public ResponseEntity<ApiResponse<List<EnquiryResponse>>> getAllEnquiries() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Enquiries retrieved successfully", enquiryService.getAllEnquiries()));
    }

}
