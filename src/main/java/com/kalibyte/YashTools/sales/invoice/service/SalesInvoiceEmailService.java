package com.kalibyte.YashTools.sales.invoice.service;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;

import java.util.List;
import java.util.UUID;

public interface SalesInvoiceEmailService {
    EmailResult sendInvoice(UUID id);
    EmailResult sendInvoice(UUID id, List<String> cc);
    EmailResult resend(UUID id);
    List<EmailLog> getDeliveryHistory(UUID id);
    EmailLog getLatestEmailLog(UUID id);
}
