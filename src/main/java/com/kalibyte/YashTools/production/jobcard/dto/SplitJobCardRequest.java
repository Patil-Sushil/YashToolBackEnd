package com.kalibyte.YashTools.production.jobcard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitJobCardRequest {

    @NotNull(message = "Job Card ID is required")
    private UUID jobCardId;

    @NotEmpty(message = "Split quantities cannot be empty")
    private List<Integer> splitQuantities;
}
