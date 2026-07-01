package com.kalibyte.YashTools.email.service.impl;

import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import com.kalibyte.YashTools.email.exception.EmailException;
import com.kalibyte.YashTools.email.repository.EmailLogRepository;
import com.kalibyte.YashTools.email.service.EmailRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryServiceImpl implements EmailRetryService {

    private final EmailLogRepository emailLogRepository;

    @Qualifier("emailServiceImpl")
    private final com.kalibyte.YashTools.email.service.EmailService emailServiceDelegate;

    @Override
    @Transactional
    public EmailResult retry(UUID emailLogId) {
        EmailLog log = emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new EmailException("EmailLog not found: " + emailLogId));
        if (!log.getMailStatus().canRetry())
            return EmailResult.failed(emailLogId, "Status not retryable: " + log.getMailStatus());
        if (!log.canRetry(log.getMaxRetries()))
            return EmailResult.failed(emailLogId, "Max retries reached");

        log.setNextRetryAt(null);
        // Re-dispatch via reflection-safe path — direct call to inner dispatch
        return ((EmailServiceImpl) emailServiceDelegate).dispatch(log);
    }

    @Override
    @Transactional
    public int retryAllPending() {
        List<EmailLog> retryable = emailLogRepository.findRetryable(EmailStatus.FAILED,
                PageRequest.of(0, 100));
        int ok = 0;
        for (EmailLog l : retryable) {
            try {
                EmailResult r = retry(l.getId());
                if (r.isSuccess()) ok++;
            } catch (Exception e) {
                log.warn("Retry failed for {}: {}", l.getId(), e.getMessage());
            }
        }
        return ok;
    }

    @Override
    public LocalDateTime computeNextRetryTime(int retryCount) {
        long delay = EmailConstants.RETRY_BACKOFF_BASE_SECONDS
                * (long) Math.pow(EmailConstants.RETRY_BACKOFF_MULTIPLIER, retryCount);
        return LocalDateTime.now().plusSeconds(delay);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getLog(UUID emailLogId) {
        return emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new EmailException("EmailLog not found: " + emailLogId));
    }
}