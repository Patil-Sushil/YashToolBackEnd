package com.example.YashToolBackEnd.enquiry.service;

import com.example.YashToolBackEnd.enquiry.dto.request.CreateEnquiryRequest;
import com.example.YashToolBackEnd.enquiry.dto.response.EnquiryResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface EnquiryService {
    @Transactional
    EnquiryResponse createEnquiry(CreateEnquiryRequest request);

    EnquiryResponse getById(Long enquiryId);

    List<EnquiryResponse> getAllEnquiries();
}
