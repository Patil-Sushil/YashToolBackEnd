package com.kalibyte.YashTools.audit.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatisticsResponse {
    private Long totalLogs;
    private Long successfulActions;
    private Long failedActions;
    private Long warningActions;
    private Long uniqueUsers;
    private Map<String, Long> actionBreakdown; // Action -> Count
    private Map<String, Long> entityBreakdown; // EntityType -> Count
    private Map<String, Long> userActivityBreakdown; // Username -> Count
    private Map<String, Long> dailyActivityBreakdown; // Date -> Count
}