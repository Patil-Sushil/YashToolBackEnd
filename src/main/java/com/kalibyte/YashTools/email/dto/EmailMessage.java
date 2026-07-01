package com.kalibyte.YashTools.email.dto;

import com.kalibyte.YashTools.email.entity.enums.EmailPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    private String entityType;
    private UUID entityId;
    private UUID companyId;
    private String companyCode;

    private String to;
    @Builder.Default private List<String> cc = new ArrayList<>();
    @Builder.Default private List<String> bcc = new ArrayList<>();

    private String replyTo;
    private String subject;
    private String bodyText;
    private String bodyHtml;

    @Builder.Default private List<EmailAttachment> attachments = new ArrayList<>();
    @Builder.Default private Map<String, String> headers = new HashMap<>();

    private EmailPriority priority;
    private boolean async = true;
    private String templateName;
    private Map<String, Object> templateVariables;
    private boolean test = false;
    private String notes;
}