package com.kalibyte.YashTools.audit.dto.request;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterRequest {

    // All fields are NULL by default
    // NULL = no filter applied = returns ALL logs

    private UUID userId;

    private String username;

    private AuditAction action;

    private AuditStatus status;

    private String entityType;

    private UUID entityId;

    private String ipAddress;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String search;
}