package com.kalibyte.YashTools.production.planning.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanningPrioritiesRequest {

    @NotEmpty(message = "Items list cannot be empty")
    private List<@NotNull Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotNull(message = "Job Card ID is required")
        private UUID jobCardId;

        @NotNull(message = "Priority is required")
        private Integer priority;
    }
}
