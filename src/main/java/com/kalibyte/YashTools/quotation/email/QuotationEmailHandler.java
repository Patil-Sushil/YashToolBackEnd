package com.kalibyte.YashTools.quotation.email;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;

import java.util.List;

public interface QuotationEmailHandler {
    EmailResult sendQuotationEmail(QuotationResponse quotation);
    EmailResult sendQuotationEmail(QuotationResponse quotation, byte[] pdfBytes);
    EmailResult sendQuotationEmail(QuotationResponse quotation, byte[] pdfBytes, List<String> ccList);
}