package com.kalibyte.YashTools.email.service.impl;

import com.kalibyte.YashTools.email.constants.EmailConstants;
import com.kalibyte.YashTools.email.dto.EmailAttachment;
import com.kalibyte.YashTools.email.exception.EmailException;
import com.kalibyte.YashTools.email.service.EmailAttachmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailAttachmentServiceImpl implements EmailAttachmentService {

    @Override
    public void validate(List<EmailAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) return;
        long total = 0;
        int idx = 0;
        for (EmailAttachment a : attachments) {
            idx++;
            long size = a.sizeBytes();
            if (size > EmailConstants.MAX_ATTACHMENT_SIZE_BYTES)
                throw new EmailException("Attachment " + idx + " (" + a.getFilename()
                        + ") exceeds " + EmailConstants.MAX_ATTACHMENT_SIZE_BYTES + " bytes");
            total += Math.max(size, 0);
            if (total > EmailConstants.MAX_TOTAL_ATTACHMENT_SIZE)
                throw new EmailException("Total attachment size exceeds "
                        + EmailConstants.MAX_TOTAL_ATTACHMENT_SIZE + " bytes");
        }
    }

    @Override
    public String summarize(List<EmailAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attachments.size(); i++) {
            if (i > 0) sb.append(" + ");
            EmailAttachment a = attachments.get(i);
            sb.append(a.getFilename());
            if (a.sizeBytes() >= 0) sb.append(" (").append(humanSize(a.sizeBytes())).append(")");
        }
        return sb.toString();
    }

    private String humanSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)) + "MB";
    }
}