package com.kalibyte.YashTools.email.dto;

import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResult {
    private UUID emailLogId;
    private EmailStatus status;
    private String providerMessageId;
    private String providerResponseCode;
    private String providerResponseMessage;
    private boolean success;

    public static EmailResult ok(UUID logId, EmailStatus status, String mid, String code, String msg) {
        boolean isOk = (status == EmailStatus.SENT || status == EmailStatus.DELIVERED || status == EmailStatus.PROCESSING || status == EmailStatus.PENDING);
        return EmailResult.builder().emailLogId(logId).status(status)
                .providerMessageId(mid).providerResponseCode(code).providerResponseMessage(msg)
                .success(isOk).build();
    }

    public static EmailResult failed(UUID logId, String reason) {
        return EmailResult.builder().emailLogId(logId).status(EmailStatus.FAILED)
                .providerResponseMessage(reason).success(false).build();
    }

    public static EmailResult fromLog(EmailLog log) {
        if (log == null) return null;
        boolean isOk = (log.getMailStatus() == EmailStatus.SENT || log.getMailStatus() == EmailStatus.DELIVERED || log.getMailStatus() == EmailStatus.PROCESSING || log.getMailStatus() == EmailStatus.PENDING);
        return EmailResult.builder()
                .emailLogId(log.getId())
                .status(log.getMailStatus())
                .providerMessageId(log.getSmtpMessageId())
                .providerResponseCode(log.getSmtpResponseCode())
                .providerResponseMessage(log.getSmtpResponseMessage() != null ? log.getSmtpResponseMessage() : log.getFailureReason())
                .success(isOk)
                .build();
    }
}