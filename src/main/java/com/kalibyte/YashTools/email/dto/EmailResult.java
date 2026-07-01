package com.kalibyte.YashTools.email.dto;

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
        return EmailResult.builder().emailLogId(logId).status(status)
                .providerMessageId(mid).providerResponseCode(code).providerResponseMessage(msg)
                .success(true).build();
    }

    public static EmailResult failed(UUID logId, String reason) {
        return EmailResult.builder().emailLogId(logId).status(EmailStatus.FAILED)
                .providerResponseMessage(reason).success(false).build();
    }
}