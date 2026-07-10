package com.kalibyte.YashTools.quotation.pricing;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.quotation.config.QuotationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Decorator that adds coating surcharge on top of the default strategy.
 */
@Component
public class CoatingPricingStrategy implements PricingStrategy {

    private final DefaultPricingStrategy base;

    public CoatingPricingStrategy(DefaultPricingStrategy base) {
        this.base = base;
    }

    @Override
    public BigDecimal computeUnitPrice(PricingContext ctx) {
        return base.computeUnitPrice(ctx);
    }

    @Override
    public boolean supports(OrderType orderType) {
        return base.supports(orderType);
    }
}