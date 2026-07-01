package com.kalibyte.YashTools.email.service.impl;

import com.kalibyte.YashTools.email.dto.EmailMessage;
import com.kalibyte.YashTools.email.exception.EmailException;
import com.kalibyte.YashTools.email.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public EmailMessage render(EmailMessage message) {
        if (message.getTemplateName() == null) return message;
        try {
            Context ctx = new Context();
            if (message.getTemplateVariables() != null)
                message.getTemplateVariables().forEach(ctx::setVariable);
            String html = templateEngine.process(message.getTemplateName(), ctx);
            message.setBodyHtml(html);
            return message;
        } catch (Exception e) {
            throw new EmailException("Template render failed: " + message.getTemplateName(), e);
        }
    }

    @Override
    public EmailMessage build(String templateName, Map<String, Object> variables,
                              String to, String subject) {
        return EmailMessage.builder()
                .templateName(templateName).templateVariables(variables)
                .to(to).subject(subject).build();
    }
}