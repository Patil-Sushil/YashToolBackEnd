package com.kalibyte.YashTools.production.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionRequest {

    @NotNull(message = "Job Card ID is required")
    private UUID jobCardId;

    @NotNull(message = "Accepted quantity is required")
    private Integer acceptedQuantity;

    @NotNull(message = "Rejected quantity is required")
    private Integer rejectedQuantity;

    @NotNull(message = "Rework quantity is required")
    private Integer reworkQuantity;

    @NotBlank(message = "Inspector name is required")
    private String inspector;

    private String remarks;
}
