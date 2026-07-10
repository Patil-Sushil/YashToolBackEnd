package com.kalibyte.YashTools.email.service;

import com.kalibyte.YashTools.email.dto.EmailAttachment;
import java.util.List;

public interface EmailAttachmentService {
    void validate(List<EmailAttachment> attachments);
    String summarize(List<EmailAttachment> attachments);
}