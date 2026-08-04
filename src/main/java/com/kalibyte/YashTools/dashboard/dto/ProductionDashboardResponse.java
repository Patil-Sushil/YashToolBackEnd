package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionDashboardResponse {
    private long runningJobsCount;
    private long completedJobsCount;
    private long delayedJobsCount;
    private long pendingJobsCount;
    private int todayProducedQuantity;
    private int todayRejectedQuantity;
    private double machineUtilizationPercentage;
    private Map<String, Long> machineStatusCounts;
}
