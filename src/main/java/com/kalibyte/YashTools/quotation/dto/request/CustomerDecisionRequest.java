package com.kalibyte.YashTools.quotation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDecisionRequest {

    @NotNull(message = "Quotation id is required")
    private UUID quotationId;

    @NotNull(message = "Decision is required (APPROVED/REJECTED/NEGOTIATION)")
    private String decision;

    private String remarks;
}