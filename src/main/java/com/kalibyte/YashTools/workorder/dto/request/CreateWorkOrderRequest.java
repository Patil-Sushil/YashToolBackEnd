package com.kalibyte.YashTools.workorder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkOrderRequest {

    @NotNull(message = "Quotation ID is required")
    private UUID quotationId;

    private String remarks;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
}
