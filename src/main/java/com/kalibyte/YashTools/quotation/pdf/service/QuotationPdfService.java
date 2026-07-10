package com.kalibyte.YashTools.quotation.pdf.service;

import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;

import java.util.UUID;

public interface QuotationPdfService {
    byte[] generatePdf(UUID quotationId);
    byte[] generatePdf(QuotationResponse quotation);
}