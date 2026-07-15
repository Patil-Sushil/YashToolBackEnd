package com.kalibyte.YashTools.production.jobcard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobCardRequest {

    @NotNull(message = "Work Order Item ID is required")
    private UUID workOrderItemId;

    private Integer quantity; // if null, defaults to remaining unscheduled quantity of the item
    private Integer priority;
    private String remarks;
}
