package com.kalibyte.YashTools.audit.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private AuditAction action;
    private String actionDescription;
    private String entityType;
    private UUID entityId;
    private String entityName;
    private String ipAddress;
    private String userAgent;
    private AuditStatus status;
    private String errorMessage;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private Long durationMs;
}