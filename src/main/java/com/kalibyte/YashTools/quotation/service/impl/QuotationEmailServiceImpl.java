package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.service.EmailRetryService;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.email.QuotationEmailHandler;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.exception.QuotationStateException;
import com.kalibyte.YashTools.quotation.exception.QuotationNotFoundException;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.QuotationEmailService;
import com.kalibyte.YashTools.quotation.pdf.service.QuotationPdfService;
import com.kalibyte.YashTools.quotation.service.QuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationEmailServiceImpl implements QuotationEmailService {

    private final QuotationService quotationService;
    private final QuotationRepository quotationRepository;
    private final QuotationSecurityService security;
    private final QuotationEmailHandler handler;
    private final QuotationPdfService pdfService;
    private final EmailService emailService;
    private final EmailRetryService emailRetryService;

    @Override
    @Transactional
    public EmailResult sendQuotation(UUID id) {
        return sendQuotation(id, List.of());
    }

    @Override
    @Transactional
    public EmailResult sendQuotation(UUID id, List<String> cc) {
        log.info("Sending quotation email for {}", id);

        Quotation entity = quotationRepository.findById(id)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found: " + id));
        if (entity.getStatus() != QuotationStatus.PRICING_READY 
                && entity.getStatus() != QuotationStatus.APPROVED
                && entity.getStatus() != QuotationStatus.SENT_TO_CUSTOMER) {
            throw new QuotationStateException("Cannot send quotation in its current status: " + entity.getStatus()
                    + ". It must be PRICING_READY, APPROVED, or already SENT_TO_CUSTOMER.");
        }

        QuotationResponse q = quotationService.getById(id);
        if (q.getCustomerEmail() == null || q.getCustomerEmail().isBlank())
            throw new QuotationStateException("Customer has no email address on file");

        byte[] pdf = pdfService.generatePdf(q);
        UUID logId = handler.sendQuotationEmail(q, pdf, cc);
        markAsSent(id);

        EmailLog row = emailService.getLog(logId);
        return EmailResult.ok(row.getId(), row.getMailStatus(),
                row.getSmtpMessageId(),
                row.getSmtpResponseCode(),
                row.getSmtpResponseMessage());
    }

    @Override
    @Transactional
    public EmailResult resend(UUID id) {
        var latest = emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_QUOTATION, id)
                .orElse(null);
        if (latest == null) return sendQuotation(id);
        return emailRetryService.retry(latest.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLog> getDeliveryHistory(UUID id) {
        security.loadForCurrentCompany(id);
        return emailService.getLogsForEntity(EmailConstants.OWNER_TYPE_QUOTATION, id);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getLatestEmailLog(UUID id) {
        security.loadForCurrentCompany(id);
        return emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_QUOTATION, id)
                .orElseThrow(() -> new QuotationStateException(
                        "No email log for quotation: " + id));
    }

    @Override
    @Transactional
    public void markAsSent(UUID id) {
        quotationRepository.findById(id).ifPresent(q -> {
            if (q.getStatus() == QuotationStatus.PRICING_READY
                    || q.getStatus() == QuotationStatus.APPROVED
                    || q.getStatus() == QuotationStatus.SENT_TO_CUSTOMER) {
                q.setStatus(QuotationStatus.SENT_TO_CUSTOMER);
                q.setSentToCustomerAt(LocalDateTime.now());
                q.setSentToCustomerBy(security.currentUsername());
                quotationRepository.save(q);
            }
        });
    }
}