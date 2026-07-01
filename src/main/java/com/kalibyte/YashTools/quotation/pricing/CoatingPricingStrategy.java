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
@RequiredArgsConstructor
public class CoatingPricingStrategy implements PricingStrategy {

    private final DefaultPricingStrategy base;
    private final QuotationProperties properties;

    @Override
    public BigDecimal computeUnitPrice(PricingContext ctx) {
        BigDecimal price = base.computeUnitPrice(ctx);

        if (properties.isAutoApplyCoatingCharge()
                && ctx.getCoatingType() != null
                && !ctx.getCoatingType().isBlank()) {
            price = price.add(properties.getDefaultCoatingCharge());
        }
        return price;
    }

    @Override
    public boolean supports(OrderType orderType) {
        return base.supports(orderType);
    }
}