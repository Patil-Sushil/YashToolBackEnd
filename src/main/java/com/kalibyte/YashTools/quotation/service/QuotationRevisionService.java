package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.quotation.dto.request.ReviseQuotationRequest;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationRevisionResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.enums.RevisionType;

import java.util.List;
import java.util.UUID;

public interface QuotationRevisionService {
    QuotationResponse createRevision(ReviseQuotationRequest request);
    List<QuotationRevisionResponse> getRevisionHistory(UUID quotationId);
    void record(Quotation prev, Quotation revised, RevisionType type, String reason);
}