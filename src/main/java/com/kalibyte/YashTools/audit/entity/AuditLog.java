package com.kalibyte.YashTools.audit.entity;




import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_user_id",     columnList = "user_id"),
        @Index(name = "idx_audit_log_username",     columnList = "username"),
        @Index(name = "idx_audit_log_action",       columnList = "action"),
        @Index(name = "idx_audit_log_timestamp",    columnList = "timestamp"),
        @Index(name = "idx_audit_log_entity_type",  columnList = "entity_type"),
        @Index(name = "idx_audit_log_entity_id",    columnList = "entity_id"),
        @Index(name = "idx_audit_log_status",       columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 255)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "action_description", length = 500)
    private String actionDescription;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuditStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_ms")
    private Long durationMs;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = AuditStatus.SUCCESS;
        }
    }
}