package com.kalibyte.YashTools.quotation.pricing;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import com.kalibyte.YashTools.quotation.util.PricingFormula;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Default pricing strategy implementing:
 * <pre>
 *   base      = (rate / 330) × overall_length
 *   unit      = base × user_multiplier
 * </pre>
 */
@Component
public class DefaultPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal computeUnitPrice(PricingContext ctx) {
        BigDecimal base = PricingFormula.computeBasePrice(
                ctx.getRatePerUnit(),
                BigDecimal.valueOf(ctx.getOverallLength()));

        BigDecimal multiplier = ctx.getUserMultiplier() == null
                ? BigDecimal.valueOf(QuotationConstants.DEFAULT_USER_MULTIPLIER)
                : ctx.getUserMultiplier();

        return PricingFormula.computeMultipliedPrice(base, multiplier);
    }

    @Override
    public boolean supports(OrderType orderType) {
        return orderType != null;
    }
}