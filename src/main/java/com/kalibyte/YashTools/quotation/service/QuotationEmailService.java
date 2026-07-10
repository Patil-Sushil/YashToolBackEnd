package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;

import java.util.List;
import java.util.UUID;

public interface QuotationEmailService {
    EmailResult sendQuotation(UUID id);
    EmailResult sendQuotation(UUID id, List<String> ccList);
    EmailResult resend(UUID id);
    List<EmailLog> getDeliveryHistory(UUID id);
    EmailLog getLatestEmailLog(UUID id);
    void markAsSent(UUID id);
}