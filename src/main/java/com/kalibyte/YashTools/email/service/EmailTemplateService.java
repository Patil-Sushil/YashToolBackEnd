package com.kalibyte.YashTools.email.service;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import java.util.Map;

public interface EmailTemplateService {
    EmailMessage render(EmailMessage message);
    EmailMessage build(String templateName, Map<String, Object> variables, String to, String subject);
}