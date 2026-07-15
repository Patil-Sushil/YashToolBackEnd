package com.kalibyte.YashTools.inventory.transaction.materialissue.dto;

import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssueRequest {

    @NotNull(message = "Item ID is required")
    private UUID itemId;

    @NotNull(message = "Material Grade ID is required")
    private UUID materialGradeId;

    @NotNull(message = "Issue Type is required")
    private MaterialIssueType issueType;

    private UUID cutPieceId;

    @NotNull(message = "Required length is required")
    @DecimalMin(value = "0.0001", message = "Required length must be greater than zero")
    private BigDecimal requiredLength;

    private BigDecimal fullRodLength;
}
