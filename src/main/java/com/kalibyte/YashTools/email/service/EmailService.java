package com.kalibyte.YashTools.email.service;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailService {
    EmailResult send(EmailMessage message);
    UUID sendAsync(EmailMessage message);
    EmailLog getLog(UUID logId);
    List<EmailLog> getLogsForEntity(String entityType, UUID entityId);
    Optional<EmailLog> findFirstByEntity(String entityType, UUID entityId);
    Page<EmailLog> search(String entityType, UUID entityId, EmailStatus status, Pageable pageable);
    UUID prepareLog(EmailMessage message);
}