package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;

import java.util.UUID;

public interface QuotationPdfService {
    byte[] generatePdf(UUID quotationId);
    byte[] generatePdf(QuotationResponse quotation);
}