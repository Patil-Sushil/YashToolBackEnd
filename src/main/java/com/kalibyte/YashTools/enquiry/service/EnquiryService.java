package com.kalibyte.YashTools.enquiry.service;

import com.kalibyte.YashTools.enquiry.dto.request.CreateEnquiryRequest;
import com.kalibyte.YashTools.enquiry.dto.response.EnquiryResponse;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface EnquiryService {
    @Transactional
    EnquiryResponse createEnquiry(CreateEnquiryRequest request);

    EnquiryResponse getById(UUID enquiryId);

    List<EnquiryResponse> getAllEnquiries();
}
