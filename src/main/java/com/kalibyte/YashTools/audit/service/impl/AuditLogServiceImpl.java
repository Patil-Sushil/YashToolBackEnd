package com.kalibyte.YashTools.audit.service.impl;


import com.kalibyte.YashTools.audit.dto.request.AuditLogFilterRequest;
import com.kalibyte.YashTools.audit.dto.response.AuditLogResponse;
import com.kalibyte.YashTools.audit.dto.response.AuditStatisticsResponse;
import com.kalibyte.YashTools.audit.dto.response.UserActivityProjection;
import com.kalibyte.YashTools.audit.dto.response.UserActivityResponse;
import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import com.kalibyte.YashTools.audit.mapper.AuditLogMapper;
import com.kalibyte.YashTools.audit.repository.AuditLogRepository;
import com.kalibyte.YashTools.audit.repository.AuditLogSpecification;
import com.kalibyte.YashTools.audit.service.AuditLogService;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ============================================================
    // GET ALL WITH FILTERS (Using Specification)
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAllLogs(
            AuditLogFilterRequest filter, Pageable pageable) {

        // ── If filter itself is null, use empty filter (no restrictions) ──
        if (filter == null) {
            filter = new AuditLogFilterRequest();
        }

        log.debug("Fetching all audit logs with filter: {}", filter);

        Specification<AuditLog> spec = AuditLogSpecification.withFilters(
                filter.getUserId(),       // null = no user filter
                filter.getUsername(),     // null = no username filter
                filter.getAction(),       // null = no action filter
                filter.getStatus(),       // null = no status filter
                filter.getEntityType(),   // null = no entityType filter
                filter.getEntityId(),     // null = no entityId filter
                filter.getIpAddress(),    // null = no ipAddress filter
                filter.getStartDate(),    // null = no startDate filter
                filter.getEndDate(),      // null = no endDate filter
                filter.getSearch()        // null = no search filter
        );

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        log.debug("Found {} audit logs (page {} of {})",
                page.getNumberOfElements(),
                page.getNumber(),
                page.getTotalPages());

        return PageResponse.from(page, auditLogMapper::toResponse);
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Audit log not found with id: " + id));
        return auditLogMapper.toResponse(auditLog);
    }

    // ============================================================
    // GET USER ACTIVITY
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getUserActivity(
            UUID userId, Pageable pageable) {

        log.debug("Fetching activity logs for userId: {}", userId);

        Page<AuditLog> page = auditLogRepository
                .findByUserIdOrderByTimestampDesc(userId, pageable);
        return PageResponse.from(page, auditLogMapper::toResponse);
    }

    // ============================================================
    // GET ENTITY AUDIT TRAIL
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getEntityAuditTrail(
            String entityType, UUID entityId) {

        log.debug("Fetching audit trail for entityType: {} entityId: {}",
                entityType, entityId);

        List<AuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByTimestampDesc(
                        entityType.toUpperCase(), entityId);
        return auditLogMapper.toResponseList(logs);
    }

    // ============================================================
    // GET STATISTICS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public AuditStatisticsResponse getStatistics(
            LocalDateTime startDate, LocalDateTime endDate) {

        log.debug("Fetching audit statistics from {} to {}", startDate, endDate);

        long total = auditLogRepository.count();

        long successful = auditLogRepository.countByStatus(AuditStatus.SUCCESS);
        long failed = auditLogRepository.countByStatus(AuditStatus.FAILURE);
        long warnings = auditLogRepository.countByStatus(AuditStatus.WARNING);

        long uniqueUsers = auditLogRepository.countUniqueUsers();

        Map<String, Long> actionBreakdown =
                auditLogRepository.countGroupedByAction()
                        .stream()
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> ((Number) row[1]).longValue(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        Map<String, Long> entityBreakdown =
                auditLogRepository.countGroupedByEntityType()
                        .stream()
                        .filter(row -> row[0] != null)
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> ((Number) row[1]).longValue(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        Map<String, Long> userActivity =
                auditLogRepository.getTopUsersByActivity()
                        .stream()
                        .filter(row -> row[0] != null)
                        .limit(10)
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> ((Number) row[1]).longValue(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        LocalDateTime since = startDate != null
                ? startDate : LocalDateTime.now().minusDays(30);

        Map<String, Long> dailyActivity =
                auditLogRepository.getDailyActivity(since)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> ((Number) row[1]).longValue(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        return AuditStatisticsResponse.builder()
                .totalLogs(total)
                .successfulActions(successful)
                .failedActions(failed)
                .warningActions(warnings)
                .uniqueUsers(uniqueUsers)
                .actionBreakdown(actionBreakdown)
                .entityBreakdown(entityBreakdown)
                .userActivityBreakdown(userActivity)
                .dailyActivityBreakdown(dailyActivity)
                .build();
    }

    // ============================================================
    // GET TOP ACTIVE USERS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getTopActiveUsers(int limit) {
        log.debug("Fetching top {} active users", limit);
        return auditLogRepository.getTopUsersByActivityProjection()
                .stream()
                .limit(limit)
                .map(this::mapToUserActivityResponse)
                .collect(Collectors.toList());
    }

    private UserActivityResponse mapToUserActivityResponse(
            UserActivityProjection projection) {
        return UserActivityResponse.builder()
                .userId(projection.getUserId())
                .username(projection.getUsername())
                .totalActions(projection.getTotalActions())
                .lastActivity(projection.getLastActivity())
                .firstActivity(projection.getFirstActivity())
                .topActions(getTopActionsByUser(projection.getUsername()))
                .build();
    }

    private List<UserActivityResponse.ActionCount> getTopActionsByUser(
            String username) {
        return auditLogRepository.getTopActionsByUser(username)
                .stream()
                .limit(5)
                .map(row -> UserActivityResponse.ActionCount.builder()
                        .action(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================================
    // GET RECENT LOGS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        log.debug("Fetching recent logs since: {}", since);
        List<AuditLog> logs = auditLogRepository
                .findByTimestampAfterOrderByTimestampDesc(since);
        return auditLogMapper.toResponseList(logs);
    }

    // ============================================================
    // EXPORT TO CSV
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public byte[] exportLogsToCSV(AuditLogFilterRequest filter) {
        log.info("Exporting audit logs to CSV with filter: {}", filter);

        if (filter == null) {
            filter = new AuditLogFilterRequest();
        }

        Specification<AuditLog> spec = AuditLogSpecification.withFilters(
                filter.getUserId(),
                filter.getUsername(),
                filter.getAction(),
                filter.getStatus(),
                filter.getEntityType(),
                filter.getEntityId(),
                filter.getIpAddress(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getSearch()
        );

        List<AuditLog> logs = auditLogRepository.findAll(spec);
        log.info("Found {} audit logs to export", logs.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(
                     baos, false, StandardCharsets.UTF_8)) {

            // CSV Header
            writer.println(
                    "ID,Timestamp,User ID,Username,Action,Status," +
                            "Entity Type,Entity Name,Entity ID," +
                            "IP Address,User Agent,Description,Duration (ms)"
            );

            // CSV Rows
            for (AuditLog auditLog : logs) {
                writer.println(String.join(",",
                        escapeCsv(auditLog.getId() != null
                                ? auditLog.getId().toString() : ""),
                        escapeCsv(auditLog.getTimestamp() != null
                                ? auditLog.getTimestamp().format(FORMATTER) : ""),
                        escapeCsv(auditLog.getUserId() != null
                                ? auditLog.getUserId().toString() : ""),
                        escapeCsv(auditLog.getUsername()),
                        escapeCsv(auditLog.getAction() != null
                                ? auditLog.getAction().name() : ""),
                        escapeCsv(auditLog.getStatus() != null
                                ? auditLog.getStatus().name() : ""),
                        escapeCsv(auditLog.getEntityType()),
                        escapeCsv(auditLog.getEntityName()),
                        escapeCsv(auditLog.getEntityId() != null
                                ? auditLog.getEntityId().toString() : ""),
                        escapeCsv(auditLog.getIpAddress()),
                        escapeCsv(auditLog.getUserAgent()),
                        escapeCsv(auditLog.getActionDescription()),
                        escapeCsv(auditLog.getDurationMs() != null
                                ? auditLog.getDurationMs().toString() : "")
                ));
            }

            writer.flush();
            byte[] csvBytes = baos.toByteArray();
            log.info("CSV export completed. Size: {} bytes", csvBytes.length);
            return csvBytes;

        } catch (Exception e) {
            log.error("Error exporting audit logs to CSV", e);
            throw new RuntimeException("Failed to export audit logs to CSV", e);
        }
    }

    // ============================================================
    // CLEANUP OLD LOGS
    // ============================================================
    @Override
    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Cleaned up audit logs older than {} days", retentionDays);
    }

    // ============================================================
    // LOG AN ACTION
    // ============================================================
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void log(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved → action: {} | user: {} | status: {}",
                    auditLog.getAction(),
                    auditLog.getUsername(),
                    auditLog.getStatus());
        } catch (Exception e) {
            // Never let audit logging break the application
            log.error("Failed to save audit log → action: {} | user: {}",
                    auditLog.getAction(),
                    auditLog.getUsername(),
                    e);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
