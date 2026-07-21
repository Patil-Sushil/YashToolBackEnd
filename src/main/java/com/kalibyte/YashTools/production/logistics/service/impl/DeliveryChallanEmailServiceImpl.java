package com.kalibyte.YashTools.production.logistics.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.service.EmailRetryService;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import com.kalibyte.YashTools.production.logistics.email.DeliveryChallanEmailHandler;
import com.kalibyte.YashTools.production.logistics.pdf.service.DeliveryChallanPdfService;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanEmailService;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryChallanEmailServiceImpl implements DeliveryChallanEmailService {

    private final DeliveryChallanService deliveryChallanService;
    private final DeliveryChallanPdfService pdfService;
    private final DeliveryChallanEmailHandler handler;
    private final EmailService emailService;
    private final EmailRetryService emailRetryService;

    @Override
    @Transactional
    public EmailResult sendChallan(UUID id) {
        return sendChallan(id, List.of());
    }

    @Override
    @Transactional
    public EmailResult sendChallan(UUID id, List<String> cc) {
        log.info("Sending delivery challan email for {}", id);

        DeliveryChallanResponse challan = deliveryChallanService.getChallanById(id);

        if (challan.getCustomerEmail() == null || challan.getCustomerEmail().isBlank()) {
            throw new BusinessException("Customer has no email address on file for Delivery Challan: " + challan.getChallanNo());
        }

        byte[] pdf = pdfService.generatePdf(challan);
        UUID logId = handler.sendChallanEmail(challan, pdf, cc);

        EmailLog row = emailService.getLog(logId);
        return EmailResult.ok(row.getId(), row.getMailStatus(),
                row.getSmtpMessageId(),
                row.getSmtpResponseCode(),
                row.getSmtpResponseMessage());
    }

    @Override
    @Transactional
    public EmailResult resend(UUID id) {
        var latest = emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_DELIVERY_CHALLAN, id)
                .orElse(null);
        if (latest == null) return sendChallan(id);
        return emailRetryService.retry(latest.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLog> getDeliveryHistory(UUID id) {
        deliveryChallanService.getChallanById(id); // security/ownership check
        return emailService.getLogsForEntity(EmailConstants.OWNER_TYPE_DELIVERY_CHALLAN, id);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getLatestEmailLog(UUID id) {
        deliveryChallanService.getChallanById(id);
        return emailService.findFirstByEntity(EmailConstants.OWNER_TYPE_DELIVERY_CHALLAN, id)
                .orElseThrow(() -> new BusinessException("No email log for Delivery Challan: " + id));
    }
}
