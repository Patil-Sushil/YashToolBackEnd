package com.kalibyte.YashTools.production.planning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Job Card ID is required")
    private UUID jobCardId;

    @NotNull(message = "Machine ID is required")
    private UUID machineId;

    @NotNull(message = "Operator ID is required")
    private Long operatorId;

    @NotBlank(message = "Shift is required")
    private String shift; // MORNING, EVENING, NIGHT

    @NotNull(message = "Planned Start Date is required")
    private LocalDate plannedStartDate;

    @NotNull(message = "Planned End Date is required")
    private LocalDate plannedEndDate;
}
