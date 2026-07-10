package com.kalibyte.YashTools.quotation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRejectDiscountRequest {

    @NotNull(message = "Approval id is required")
    private UUID approvalId;

    @NotNull(message = "Decision is required (APPROVED/REJECTED)")
    private String decision;   // APPROVED | REJECTED

    private String comments;
}