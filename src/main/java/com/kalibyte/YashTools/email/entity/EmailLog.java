package com.kalibyte.YashTools.email.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "email_log",
        indexes = {
                @Index(name = "idx_email_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_email_owner", columnList = "entity_id"),
                @Index(name = "idx_email_company", columnList = "company_id"),
                @Index(name = "idx_email_status", columnList = "mail_status"),
                @Index(name = "idx_email_message_id", columnList = "smtp_message_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailLog extends AuditableEntity {

    @Column(name = "company_id") private UUID companyId;
    @Column(name = "company_code", length = 20) private String companyCode;

    @Column(name = "entity_type", nullable = false, length = 50) private String entityType;
    @Column(name = "entity_id", nullable = false) private UUID entityId;

    @Column(name = "from_address", nullable = false) private String fromAddress;
    @Column(name = "to_addresses", nullable = false, columnDefinition = "TEXT") private String toAddresses;
    @Column(name = "cc_addresses", columnDefinition = "TEXT") private String ccAddresses;
    @Column(name = "bcc_addresses", columnDefinition = "TEXT") private String bccAddresses;

    @Column(nullable = false, length = 1000) private String subject;
    @Column(name = "body_text", columnDefinition = "TEXT") private String bodyText;
    @Column(name = "body_html", columnDefinition = "TEXT") private String bodyHtml;
    @Column(length = 20) @Builder.Default private String charset = "UTF-8";

    @Column(name = "has_attachments", nullable = false) @Builder.Default private Boolean hasAttachments = false;
    @Column(name = "attachment_count", nullable = false) @Builder.Default private Integer attachmentCount = 0;
    @Column(name = "attachment_total_size_bytes", nullable = false) @Builder.Default private Long attachmentTotalSizeBytes = 0L;
    @Column(name = "attachment_summary", length = 500) private String attachmentSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "mail_status", nullable = false, length = 20) @Builder.Default private EmailStatus mailStatus = EmailStatus.PENDING;
    @Column(name = "provider_type", nullable = false, length = 20) @Builder.Default private String providerType = "SMTP";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10) @Builder.Default private EmailPriority priority = EmailPriority.NORMAL;

    @Column(name = "smtp_message_id") private String smtpMessageId;
    @Column(name = "smtp_response_code", length = 50) private String smtpResponseCode;
    @Column(name = "smtp_response_message", columnDefinition = "TEXT") private String smtpResponseMessage;

    @Column(name = "queued_at", nullable = false) @Builder.Default private LocalDateTime queuedAt = LocalDateTime.now();
    @Column(name = "sent_at") private LocalDateTime sentAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;

    @Column(name = "retry_count", nullable = false) @Builder.Default private Integer retryCount = 0;
    @Column(name = "max_retries", nullable = false) @Builder.Default private Integer maxRetries = 3;
    @Column(name = "last_retry_at") private LocalDateTime lastRetryAt;
    @Column(name = "next_retry_at") private LocalDateTime nextRetryAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT") private String failureReason;
    @Column(name = "failure_stack_trace", columnDefinition = "TEXT") private String failureStackTrace;

    @Column(name = "is_test", nullable = false) @Builder.Default private Boolean isTest = false;

    @OneToMany(mappedBy = "emailLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<EmailRecipient> recipients = new ArrayList<>();

    @OneToMany(mappedBy = "emailLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<EmailAttachmentLog> attachmentLogs = new ArrayList<>();

    public boolean canRetry(int maxAllowed) {
        return retryCount < Math.min(maxRetries, maxAllowed);
    }

    public void markProcessing() { this.mailStatus = EmailStatus.PROCESSING; }

    public void markSent(String messageId, String code, String message) {
        this.mailStatus = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.smtpMessageId = messageId;
        this.smtpResponseCode = code != null && code.length() > 50 ? code.substring(0, 50) : code;
        this.smtpResponseMessage = message;
    }

    public void markFailed(String reason, Throwable error) {
        this.mailStatus = EmailStatus.FAILED;
        this.failureReason = reason;
        if (error != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(error.getClass().getName()).append(": ").append(error.getMessage()).append("\n");
            int i = 0;
            for (StackTraceElement el : error.getStackTrace()) {
                if (i++ > 30) break;
                sb.append("\tat ").append(el).append("\n");
            }
            this.failureStackTrace = sb.toString();
        }
        this.retryCount += 1;
        this.lastRetryAt = LocalDateTime.now();
    }

    public void addRecipient(EmailRecipient r) { recipients.add(r); r.setEmailLog(this); }
    public void addAttachmentLog(EmailAttachmentLog a) { attachmentLogs.add(a); a.setEmailLog(this); }

    public void warn(String s, UUID id, String message) {
        
    }

    public void info(String s, UUID id, String subject) {
    }
}