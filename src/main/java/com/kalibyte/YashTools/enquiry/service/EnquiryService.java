package com.kalibyte.YashTools.enquiry.service;

import com.kalibyte.YashTools.enquiry.dto.request.CreateEnquiryRequest;
import com.kalibyte.YashTools.enquiry.dto.request.UpdateEnquiryStatusRequest;
import com.kalibyte.YashTools.enquiry.dto.response.EnquiryResponse;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface EnquiryService {

    @Transactional
    EnquiryResponse createEnquiry(CreateEnquiryRequest request);

    EnquiryResponse getById(UUID enquiryId);

    List<EnquiryResponse> getAllEnquiries();

    List<EnquiryResponse> getEnquiriesByCustomer(UUID customerId);

    /**
     * Update enquiry status with transition validation
     *
     * @param enquiryId the enquiry identifier
     * @param request status update request
     * @return updated enquiry details
     */
    @Transactional
    EnquiryResponse updateEnquiryStatus(UUID enquiryId, UpdateEnquiryStatusRequest request);
}