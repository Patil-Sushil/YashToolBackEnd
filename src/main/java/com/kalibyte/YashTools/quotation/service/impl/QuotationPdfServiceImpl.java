package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.QuotationPdfService;
import com.kalibyte.YashTools.quotation.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationPdfServiceImpl implements QuotationPdfService {

    private final QuotationService quotationService;
    private final QuotationSecurityService security;

    @Override
    public byte[] generatePdf(UUID id) {
        security.loadForCurrentCompany(id);
        return generatePdf(quotationService.getById(id));
    }

    @Override
    public byte[] generatePdf(QuotationResponse quotation) {
        log.info("Generating PDF for quotation {}", quotation.getQuotationNo());
        // Placeholder: integrate OpenPDF/iText in production
        String text = String.format("Quotation %s — Total: %s — Customer: %s",
                quotation.getQuotationNo(),
                quotation.getGrandTotal(),
                quotation.getCustomerCompanyName());
        return text.getBytes();
    }
}