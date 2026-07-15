package com.kalibyte.YashTools.production.execution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {

    @NotNull(message = "Produced quantity is required")
    private Integer producedQuantity;

    @NotNull(message = "Rejected quantity is required")
    private Integer rejectedQuantity;

    @NotNull(message = "Rework quantity is required")
    private Integer reworkQuantity;

    private Integer machineDowntimeMinutes;
    private String downtimeReason;
    private String remarks;
    
    private Boolean isShiftComplete; // if true, ends the execution log session (sets endTime)
}
