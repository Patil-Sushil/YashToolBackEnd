package com.kalibyte.YashTools.quotation.pricing;

import com.kalibyte.YashTools.common.enums.OrderType;
import java.math.BigDecimal;

public interface PricingStrategy {

    /** Compute unit price for a single piece. */
    BigDecimal computeUnitPrice(PricingContext ctx);

    /** Whether this strategy supports the given order type. */
    boolean supports(OrderType orderType);
}