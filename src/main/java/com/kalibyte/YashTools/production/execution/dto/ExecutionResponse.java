package com.kalibyte.YashTools.production.execution.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private UUID id;
    private UUID jobCardId;
    private String jobCardNo;
    private Long operatorId;
    private String operatorName;
    private UUID machineId;
    private String machineCode;
    private String shift;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer targetQuantity;
    private Integer producedQuantity;
    private Integer rejectedQuantity;
    private Integer reworkQuantity;
    private Integer pendingQuantity;
    private Integer machineDowntimeMinutes;
    private String downtimeReason;
    private String remarks;
    private LocalDateTime createdAt;
    private String createdBy;
}
