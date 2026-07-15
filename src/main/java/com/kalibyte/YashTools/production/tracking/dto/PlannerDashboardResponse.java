package com.kalibyte.YashTools.production.tracking.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerDashboardResponse {
    private long runningJobsCount;
    private long completedJobsCount;
    private long delayedJobsCount;
    private long pendingJobsCount;
    private int todayProducedQuantity;
    private int todayRejectedQuantity;
    private double machineUtilizationPercentage;
    private Map<String, Long> machineStatusCounts; // e.g. "ACTIVE": 5, "UNDER_MAINTENANCE": 1
}
