package com.kalibyte.YashTools.sales.invoice.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.service.EmailRetryService;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import com.kalibyte.YashTools.sales.invoice.email.SalesInvoiceEmailHandler;
import com.kalibyte.YashTools.sales.invoice.pdf.service.SalesInvoicePdfService;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceEmailService;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceEmailServiceImpl implements SalesInvoiceEmailService {

    private final SalesInvoiceService salesInvoiceService;
    private final SalesInvoicePdfService pdfService;
    private final SalesInvoiceEmailHandler handler;
    private final EmailService emailService;
    private final EmailRetryService emailRetryService;

    @Override
    @Transactional
    public EmailResult sendInvoice(UUID id) {
        return sendInvoice(id, List.of());
    }

    @Override
    @Transactional
    public EmailResult sendInvoice(UUID id, List<String> cc) {
        log.info("Sending sales invoice email for {}", id);

        SalesInvoiceResponse invoice = salesInvoiceService.getInvoiceById(id);

        if (invoice.getCustomerEmail() == null || invoice.getCustomerEmail().isBlank()) {
            throw new BusinessException("Customer has no email address on file for Sales Invoice: " + invoice.getInvoiceNo());
        }

        byte[] pdf = pdfService.generatePdf(invoice);
        UUID logId = handler.sendInvoiceEmail(invoice, pdf, cc);

        EmailLog row = emailService.getLog(logId);
        return EmailResult.fromLog(row);
    }

    @Override
    @Transactional
    public EmailResult resend(UUID id) {
        var latest = emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_INVOICE, id)
                .orElse(null);
        if (latest == null) return sendInvoice(id);
        return emailRetryService.retry(latest.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLog> getDeliveryHistory(UUID id) {
        salesInvoiceService.getInvoiceById(id); // security/ownership check
        return emailService.getLogsForEntity(EmailConstants.OWNER_TYPE_INVOICE, id);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getLatestEmailLog(UUID id) {
        salesInvoiceService.getInvoiceById(id);
        return emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_INVOICE, id)
                .orElseThrow(() -> new BusinessException("No email log for Sales Invoice: " + id));
    }
}
