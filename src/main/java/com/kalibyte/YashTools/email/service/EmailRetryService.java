package com.kalibyte.YashTools.email.service;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EmailRetryService {
    EmailResult retry(UUID emailLogId);
    int retryAllPending();
    LocalDateTime computeNextRetryTime(int retryCount);
    EmailLog getLog(UUID emailLogId);
}