package com.kalibyte.YashTools.quotation.pricing;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PricingStrategyFactory {

    private final List<PricingStrategy> strategies;

    public PricingStrategy resolve(OrderType orderType) {
        return strategies.stream()
                .filter(s -> s.supports(orderType))
                .findFirst()
                .orElseThrow(() -> new PricingException(
                        "No pricing strategy for order type: " + orderType));
    }
}