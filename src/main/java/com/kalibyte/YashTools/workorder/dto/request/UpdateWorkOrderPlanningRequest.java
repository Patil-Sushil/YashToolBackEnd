package com.kalibyte.YashTools.workorder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderPlanningRequest {

    @NotNull(message = "Planned start date is required")
    private LocalDate plannedStartDate;

    @NotNull(message = "Planned end date is required")
    private LocalDate plannedEndDate;
}
