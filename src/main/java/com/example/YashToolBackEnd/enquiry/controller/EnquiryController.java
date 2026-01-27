package com.example.YashToolBackEnd.enquiry.controller;

import com.example.YashToolBackEnd.enquiry.dto.request.CreateEnquiryRequest;
import com.example.YashToolBackEnd.enquiry.dto.response.EnquiryResponse;
import com.example.YashToolBackEnd.enquiry.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {
    private final EnquiryService enquiryService;

    // Create  new Enquiry
    @PostMapping
    public EnquiryResponse createEmquiry(@RequestBody CreateEnquiryRequest request) {
        return enquiryService.createEnquiry(request);

    }

    // Get enquiry by ID
    @GetMapping("/{id}")
    public EnquiryResponse getById(@PathVariable Long id) {
        return enquiryService.getById(id);
    }

    // Getting All Enquiries
    @GetMapping
    public List<EnquiryResponse> getAllEnquiries() {
        return enquiryService.getAllEnquiries();
    }

}
