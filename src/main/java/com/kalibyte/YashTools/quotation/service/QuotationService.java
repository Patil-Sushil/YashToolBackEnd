package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.quotation.dto.request.*;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QuotationService {
    QuotationResponse createFromEnquiry(CreateFromEnquiryRequest request);
    QuotationResponse createDirect(CreateDirectQuotationRequest request);
    QuotationResponse getById(UUID id);
    QuotationResponse getByNumber(String quotationNo);
    QuotationResponse updateDraft(UUID id, UpdateQuotationRequest request);
    QuotationResponse revise(ReviseQuotationRequest request);
    QuotationResponse lockFinal(UUID id);
    QuotationResponse cancel(UUID id, String reason);
    Page<QuotationResponse> listForCurrentCompany(String status, Pageable pageable);
    QuotationResponse recordCustomerDecision(UUID id, String decision, String remarks);


    Page<QuotationResponse> searchQuotations(String query, Pageable pageable);

}