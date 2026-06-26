package com.kalibyte.YashTools.audit.service;


import com.kalibyte.YashTools.audit.dto.request.AuditLogFilterRequest;
import com.kalibyte.YashTools.audit.dto.response.AuditLogResponse;
import com.kalibyte.YashTools.audit.dto.response.AuditStatisticsResponse;
import com.kalibyte.YashTools.audit.dto.response.UserActivityResponse;
import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    /**
     * Get all audit logs with filters and pagination
     */
    PageResponse<AuditLogResponse> getAllLogs(AuditLogFilterRequest filter, Pageable pageable);

    /**
     * Get audit log by ID
     */
    AuditLogResponse getLogById(UUID id);

    /**
     * Get user activity logs
     */
    PageResponse<AuditLogResponse> getUserActivity(UUID userId, Pageable pageable);

    /**
     * Get entity-specific audit trail
     */
    List<AuditLogResponse> getEntityAuditTrail(String entityType, UUID entityId);

    /**
     * Get audit statistics
     */
    AuditStatisticsResponse getStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get top active users
     */
    List<UserActivityResponse> getTopActiveUsers(int limit);

    /**
     * Get recent logs
     */
    List<AuditLogResponse> getRecentLogs(int hours);

    /**
     * Export logs to CSV
     */
    byte[] exportLogsToCSV(AuditLogFilterRequest filter);

    /**
     * Cleanup old logs (retention policy)
     */
    void cleanupOldLogs(int retentionDays);

    /**
     * Create audit log entry
     */
    void log(AuditLog auditLog);
}