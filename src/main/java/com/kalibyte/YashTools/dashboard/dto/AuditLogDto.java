package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private String username;
    private String action;
    private String actionDescription;
    private String entityType;
    private String status;
    private LocalDateTime timestamp;
}
