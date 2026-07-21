package com.kalibyte.YashTools.production.planning.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private UUID id;
    private UUID jobCardId;
    private String jobCardNo;
    private Integer totalQuantity;
    private UUID machineId;
    private String machineCode;
    private String machineName;
    private Long operatorId;
    private String operatorName;
    private String shift;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private UUID companyId;
}
