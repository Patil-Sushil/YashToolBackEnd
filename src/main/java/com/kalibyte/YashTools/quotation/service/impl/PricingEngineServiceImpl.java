package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.quotation.client.RateChartClient;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import com.kalibyte.YashTools.quotation.pricing.PricingContext;
import com.kalibyte.YashTools.quotation.pricing.PricingStrategy;
import com.kalibyte.YashTools.quotation.pricing.PricingStrategyFactory;
import com.kalibyte.YashTools.quotation.service.PricingEngineService;
import com.kalibyte.YashTools.quotation.util.PricingFormula;
import com.kalibyte.YashTools.quotation.validator.QuotationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingEngineServiceImpl implements PricingEngineService {

    private final RateChartClient rateChartClient;
    private final PricingStrategyFactory strategyFactory;
    private final QuotationValidator validator;

    @Override
    @Transactional(readOnly = true)
    public PricingBreakdown priceItem(QuotationItemRequest req) {
        log.debug("Pricing item: tool={}, grade={}, length={}, multiplier={}, qty={}",
                req.getToolName(), req.getMaterialGrade(),
                req.getOverallLength(), req.getUserMultiplier(), req.getQuantity());

        validator.validateItem(req, 1);

        if (req.getOrderType() == OrderType.NEW_TOOL && req.getMaterialGrade() == null)
            throw new PricingException("materialGrade required for NEW_TOOL");

        PricingContext initial = PricingContext.builder()
                .orderType(req.getOrderType())
                .toolName(req.getToolName())
                .itemName(req.getItemName())
                .materialGrade(req.getMaterialGrade())
                .quantity(req.getQuantity())
                .trial(req.getTrial())
                .overallLength(req.getOverallLength())
                .userMultiplier(req.getUserMultiplier() == null
                        ? BigDecimal.valueOf(QuotationConstants.DEFAULT_USER_MULTIPLIER)
                        : req.getUserMultiplier())
                .coatingType(req.getSpecs() != null && req.getSpecs().getCoatingType() != null
                        ? req.getSpecs().getCoatingType().name() : null)
                .interState(false)
                .build();

        PricingContext enriched = rateChartClient.enrichContext(initial);
        PricingStrategy strategy = strategyFactory.resolve(enriched.getOrderType());
        BigDecimal unitPrice = strategy.computeUnitPrice(enriched);

        if (unitPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new PricingException("Computed negative unit price");

        BigDecimal lineSubtotal = PricingFormula.computeLineSubtotal(
                unitPrice, enriched.getQuantity());

        BigDecimal base = unitPrice.divide(enriched.getUserMultiplier(), 2, RoundingMode.HALF_UP);

        return PricingBreakdown.builder()
                .ratePerUnit(enriched.getRatePerUnit())
                .rateChartItem(enriched.getRateChartItem())
                .standardRodLengthMm(QuotationConstants.STANDARD_ROD_LENGTH_MM)
                .actualLengthMm(enriched.getOverallLength())
                .userMultiplier(enriched.getUserMultiplier())
                .basePrice(base)
                .multipliedPrice(unitPrice)
                .coatingCharge(BigDecimal.ZERO)
                .unitPrice(unitPrice)
                .lineSubtotal(lineSubtotal)
                .rateSourceTable(rateChartClient.sourceTable())
                .build();
    }

    @Override
    @Transactional
    public void recomputeQuotation(UUID quotationId) {
        log.info("Recomputing pricing for quotation {}", quotationId);
    }
}