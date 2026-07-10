package com.kalibyte.YashTools.email.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_recipient", indexes = {
        @Index(name = "idx_recipient_log", columnList = "email_log_id"),
        @Index(name = "idx_recipient_address", columnList = "recipient_address")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailRecipient extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_log_id", nullable = false)
    private EmailLog emailLog;

    @Column(name = "recipient_address", nullable = false) private String recipientAddress;
    @Column(name = "recipient_type", nullable = false, length = 10) private String recipientType;
    @Column(name = "delivery_status", nullable = false, length = 20) @Builder.Default private String deliveryStatus = "PENDING";
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @Column(name = "opened_at") private LocalDateTime openedAt;
    @Column(name = "clicked_at") private LocalDateTime clickedAt;
    @Column(name = "bounced_at") private LocalDateTime bouncedAt;
    @Column(name = "bounce_reason", columnDefinition = "TEXT") private String bounceReason;
}