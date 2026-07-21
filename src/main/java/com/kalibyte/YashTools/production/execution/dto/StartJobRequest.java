package com.kalibyte.YashTools.production.execution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartJobRequest {

    @NotNull(message = "Job Card ID is required")
    private UUID jobCardId;

    @NotNull(message = "Operator ID is required")
    private Long operatorId;

    @NotNull(message = "Machine ID is required")
    private UUID machineId;

    @NotBlank(message = "Shift is required")
    private String shift; // MORNING, EVENING, NIGHT

    @NotNull(message = "Target Quantity is required")
    private Integer targetQuantity;
}
