package com.kalibyte.YashTools.email.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "email_attachment_log", indexes = {
        @Index(name = "idx_attachment_log", columnList = "email_log_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailAttachmentLog extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_log_id", nullable = false)
    private EmailLog emailLog;

    @Column(nullable = false, length = 500) private String filename;
    @Column(name = "content_type", length = 100) private String contentType;
    @Column(name = "size_bytes", nullable = false) private Long sizeBytes;
    @Column(name = "storage_path", length = 1000) private String storagePath;
    @Column(nullable = false) @Builder.Default private Boolean inline = false;
    @Column(name = "content_id") private String contentId;
}