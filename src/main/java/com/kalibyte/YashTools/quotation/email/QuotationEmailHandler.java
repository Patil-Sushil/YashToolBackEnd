package com.kalibyte.YashTools.quotation.email;

import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;

import java.util.List;
import java.util.UUID;

public interface QuotationEmailHandler {
    UUID sendQuotationEmail(QuotationResponse quotation);
    UUID sendQuotationEmail(QuotationResponse quotation, byte[] pdfBytes);
    UUID sendQuotationEmail(QuotationResponse quotation, byte[] pdfBytes, List<String> ccList);
}