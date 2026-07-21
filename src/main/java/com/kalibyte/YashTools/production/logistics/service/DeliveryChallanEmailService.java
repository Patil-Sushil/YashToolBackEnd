package com.kalibyte.YashTools.production.logistics.service;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;

import java.util.List;
import java.util.UUID;

public interface DeliveryChallanEmailService {
    EmailResult sendChallan(UUID id);
    EmailResult sendChallan(UUID id, List<String> cc);
    EmailResult resend(UUID id);
    List<EmailLog> getDeliveryHistory(UUID id);
    EmailLog getLatestEmailLog(UUID id);
}
