package com.kalibyte.YashTools.quotation.pricing;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Immutable context passed into pricing strategies.
 * Built once, never mutated.
 */
@Data
@Builder
public class PricingContext {

    private final OrderType orderType;
    private final String toolName;
    private final String itemName;
    private final MaterialGrade materialGrade;
    private final Integer quantity;
    private final Boolean trial;
    private final Double overallLength;
    private final BigDecimal userMultiplier;
    private final String coatingType;
    private final BigDecimal ratePerUnit;
    private final String rateChartItem;
    private final boolean interState;
}