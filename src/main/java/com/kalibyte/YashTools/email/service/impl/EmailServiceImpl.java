package com.kalibyte.YashTools.email.service.impl;

import com.kalibyte.YashTools.email.config.EmailProperties;
import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailAttachmentLog;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.EmailRecipient;
import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import com.kalibyte.YashTools.email.exception.EmailException;
import com.kalibyte.YashTools.email.repository.EmailLogRepository;
import com.kalibyte.YashTools.email.service.EmailAttachmentService;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.email.service.EmailTemplateService;
import com.kalibyte.YashTools.email.util.EmailAddressValidator;
import com.kalibyte.YashTools.email.util.EmailMimeUtils;
import com.kalibyte.YashTools.email.util.EmailStorageService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final EmailAttachmentService attachmentService;
    private final EmailTemplateService templateService;
    private final EmailStorageService storageService;
    private final com.kalibyte.YashTools.company.repository.CompanyRepository companyRepository;
    private final EmailServiceImpl self;

    public EmailServiceImpl(
            EmailLogRepository emailLogRepository,
            JavaMailSender mailSender,
            EmailProperties emailProperties,
            EmailAttachmentService attachmentService,
            EmailTemplateService templateService,
            EmailStorageService storageService,
            com.kalibyte.YashTools.company.repository.CompanyRepository companyRepository,
            @Lazy EmailServiceImpl self) {
        this.emailLogRepository = emailLogRepository;
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.attachmentService = attachmentService;
        this.templateService = templateService;
        this.storageService = storageService;
        this.companyRepository = companyRepository;
        this.self = self;
    }

    // =================== PUBLIC ===================

    @Override
    public EmailResult send(EmailMessage message) {
        validate(message);
        EmailLog log = self.prepareLogTx(message);
        return self.dispatch(log);
    }

    @Override
    @Async("emailExecutor")
    public UUID sendAsync(EmailMessage message) {
        EmailResult r = send(message);
        if (!r.isSuccess()) log.warn("Async send failed: {}", r.getProviderResponseMessage());
        return r.getEmailLogId();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getLog(UUID logId) {
        return emailLogRepository.findById(logId)
                .orElseThrow(() -> new EmailException("Email log not found: " + logId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailLog> getLogsForEntity(String entityType, UUID entityId) {
        return emailLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailLog> findFirstByEntity(String entityType, UUID entityId) {
        return emailLogRepository.findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailLog> search(String entityType, UUID entityId,
                                 EmailStatus status, Pageable pageable) {
        return emailLogRepository.findAll(
                com.kalibyte.YashTools.email.repository.EmailLogSpecification.withEntity(entityType, entityId)
                        .and(com.kalibyte.YashTools.email.repository.EmailLogSpecification.withStatus(status)),
                pageable);
    }

    @Override
    @Transactional
    public UUID prepareLog(EmailMessage message) {
        validate(message);
        return self.prepareLogTx(message).getId();
    }

    // =================== INTERNAL ===================

    @Transactional(propagation = Propagation.REQUIRED)
    public EmailLog prepareLogTx(EmailMessage message) {
        EmailLog log = EmailLog.builder()
                .entityType(message.getEntityType())
                .entityId(message.getEntityId())
                .companyId(message.getCompanyId())
                .companyCode(message.getCompanyCode())
                .fromAddress(emailProperties.getFromAddress())
                .toAddresses(message.getTo())
                .ccAddresses(message.getCc() == null || message.getCc().isEmpty() ? null : String.join(",", message.getCc()))
                .bccAddresses(message.getBcc() == null || message.getBcc().isEmpty() ? null : String.join(",", message.getBcc()))
                .subject(message.getSubject())
                .bodyText(message.getBodyText())
                .charset("UTF-8")
                .priority(message.getPriority() == null ? EmailPriority.NORMAL : message.getPriority())
                .providerType(EmailConstants.PROVIDER_SMTP)
                .maxRetries(emailProperties.getMaxRetries())
                .isTest(message.isTest())
                .build();

        if (message.getTo() != null) Arrays.stream(message.getTo().split(",")).map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(addr -> log.addRecipient(EmailRecipient.builder()
                        .recipientAddress(addr).recipientType("TO").build()));
        if (message.getCc() != null) message.getCc().forEach(addr ->
                log.addRecipient(EmailRecipient.builder().recipientAddress(addr).recipientType("CC").build()));
        if (message.getBcc() != null) message.getBcc().forEach(addr ->
                log.addRecipient(EmailRecipient.builder().recipientAddress(addr).recipientType("BCC").build()));

        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            attachmentService.validate(message.getAttachments());
            log.setHasAttachments(true);
            log.setAttachmentCount(message.getAttachments().size());
            log.setAttachmentSummary(attachmentService.summarize(message.getAttachments()));
            long total = message.getAttachments().stream().mapToLong(EmailAttachment::sizeBytes)
                    .filter(s -> s >= 0).sum();
            log.setAttachmentTotalSizeBytes(total);
            emailLogRepository.save(log); // Save to generate ID for storage path folder
            message.getAttachments().forEach(a ->
                    log.addAttachmentLog(EmailAttachmentLog.builder()
                            .filename(a.getFilename())
                            .contentType(a.getContentType() != null ? a.getContentType()
                                    : EmailMimeUtils.mimeType(a.getFilename()))
                            .sizeBytes(a.sizeBytes() < 0 ? 0 : a.sizeBytes())
                            .storagePath(storageService.persist(log.getId(), a))
                            .inline(a.isInline())
                            .build()));
        }

        if (message.getTemplateName() != null) {
            templateService.render(message);
            log.setBodyHtml(message.getBodyHtml());
        }
        return emailLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EmailResult dispatch(EmailLog emailLog) {
        if (!emailProperties.isEnabled()) {
            log.warn("Email module disabled, logId={}", emailLog.getId());
            return EmailResult.failed(emailLog.getId(), "Email module disabled");
        }
        emailLog.markProcessing();

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            populateMime(mime, emailLog);
            mailSender.send(mime);

            emailLog.markSent(mime.getMessageID(), "250", "Message accepted for delivery");
            emailLogRepository.save(emailLog);
            log.info("SMTP send OK logId={} subject='{}'", emailLog.getId(), emailLog.getSubject());

            return EmailResult.fromLog(emailLog);
        } catch (Exception e) {
            emailLog.markFailed(e.getMessage(), e);
            emailLogRepository.save(emailLog);
            log.warn("SMTP send FAILED logId={} reason={}", emailLog.getId(), e.getMessage());
            return EmailResult.fromLog(emailLog);
        }
    }

    private void populateMime(MimeMessage mime, EmailLog emailLog) throws Exception {
        String fromName = emailProperties.getFromDisplayName();
        if (emailLog.getCompanyId() != null) {
            fromName = companyRepository.findById(emailLog.getCompanyId())
                    .map(com.kalibyte.YashTools.company.entity.Company::getName)
                    .orElse(fromName);
        }

        mime.setFrom(new InternetAddress(emailProperties.getFromAddress(),
                fromName, StandardCharsets.UTF_8.name()));
        if (emailLog.getToAddresses() != null) mime.setRecipients(MimeMessage.RecipientType.TO,
                InternetAddress.parse(emailLog.getToAddresses(), false));
        if (emailLog.getCcAddresses() != null && !emailLog.getCcAddresses().isBlank())
            mime.setRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(emailLog.getCcAddresses(), false));
        if (emailLog.getBccAddresses() != null && !emailLog.getBccAddresses().isBlank())
            mime.setRecipients(MimeMessage.RecipientType.BCC, InternetAddress.parse(emailLog.getBccAddresses(), false));
        mime.setSubject(emailLog.getSubject(), StandardCharsets.UTF_8.name());

        if (emailLog.getEntityType() != null) mime.setHeader(EmailConstants.HEADER_X_ENTITY_TYPE, emailLog.getEntityType());
        if (emailLog.getEntityId() != null) mime.setHeader(EmailConstants.HEADER_X_ENTITY_ID, emailLog.getEntityId().toString());

        MimeMultipart multipart = new MimeMultipart("mixed");
        MimeBodyPart body = new MimeBodyPart();
        boolean hasHtml = emailLog.getBodyHtml() != null && !emailLog.getBodyHtml().isBlank();
        if (hasHtml) body.setContent(emailLog.getBodyHtml(), "text/html; charset=UTF-8");
        else body.setText(emailLog.getBodyText() == null ? "" : emailLog.getBodyText(),
                StandardCharsets.UTF_8.name());
        multipart.addBodyPart(body);

        if (emailLog.getHasAttachments() && emailLog.getAttachmentLogs() != null) {
            for (EmailAttachmentLog a : emailLog.getAttachmentLogs()) {
                if (a.getStoragePath() == null) continue;
                byte[] bytes = storageService.read(a.getStoragePath());
                MimeBodyPart part = new MimeBodyPart();
                part.setContent(bytes, a.getContentType() != null
                        ? a.getContentType() : "application/octet-stream");
                part.setFileName(a.getFilename());
                part.setDisposition(MimeBodyPart.ATTACHMENT);
                multipart.addBodyPart(part);
            }
        }
        mime.setContent(multipart);
        mime.saveChanges();
    }

    private void validate(EmailMessage message) {
        if (message == null) throw new EmailException("EmailMessage is null");
        if (message.getEntityType() == null || message.getEntityId() == null)
            throw new EmailException("entityType and entityId required");
        if (message.getSubject() == null || message.getSubject().isBlank())
            throw new EmailException("Subject required");
        if ((message.getBodyText() == null || message.getBodyText().isBlank())
                && (message.getBodyHtml() == null || message.getBodyHtml().isBlank())
                && message.getTemplateName() == null)
            throw new EmailException("Either bodyText, bodyHtml or templateName required");
        if (message.getTo() == null || message.getTo().isBlank())
            throw new EmailException("Recipient required");
        for (String addr : message.getTo().split(",")) EmailAddressValidator.requireValid(addr.trim());
        if (message.getCc() != null) message.getCc().forEach(EmailAddressValidator::requireValid);
        if (message.getBcc() != null) message.getBcc().forEach(EmailAddressValidator::requireValid);
    }
}