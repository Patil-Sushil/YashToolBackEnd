package com.kalibyte.YashTools.quotation.dto.request;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationItemRequest {

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotBlank(message = "Tool name is required")
    private String toolName;

    @NotBlank(message = "Item name is required")
    private String itemName;

    private MaterialGrade materialGrade;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Builder.Default
    private Boolean trial = false;

    @NotNull
    @Positive(message = "Overall length must be > 0")
    private Double overallLength;

    private String coatingType;

    @NotNull(message = "Multiplier is required (use 1.0 if no multiplier needed)")
    @Positive(message = "Multiplier must be > 0")
    @Builder.Default
    private BigDecimal userMultiplier = BigDecimal.ONE;

    private QuotationItemSpecsRequest specs;
    private String remarks;
    private String drawingReference;
}