package com.kalibyte.YashTools.report.dto;

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
public class ProductionExecutionDetailDTO {
    private UUID logId;
    private String jobCardNo;
    private String operatorName;
    private String machineName;
    private String shift;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer targetQuantity;
    private Integer producedQuantity;
    private Integer rejectedQuantity;
    private Integer reworkQuantity;
    private Integer downtimeMinutes;
    private String downtimeReason;
}
