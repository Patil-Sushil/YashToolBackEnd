package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import com.kalibyte.YashTools.master.ratechart.service.ToolServiceRateMasterService;
import com.kalibyte.YashTools.quotation.client.RateChartClient;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import com.kalibyte.YashTools.quotation.pricing.PricingContext;
import com.kalibyte.YashTools.quotation.pricing.PricingStrategy;
import com.kalibyte.YashTools.quotation.pricing.PricingStrategyFactory;
import com.kalibyte.YashTools.quotation.service.PricingEngineService;
import com.kalibyte.YashTools.quotation.config.QuotationProperties;
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
    private final ToolServiceRateMasterService toolServiceRateMasterService;
    private final QuotationProperties properties;

    @Override
    @Transactional(readOnly = true)
    public PricingBreakdown priceItem(QuotationItemRequest req) {
        log.debug("Pricing item: tool={}, grade={}, length={}, multiplier={}, qty={}",
                req.getToolName(), req.getMaterialGrade(),
                req.getOverallLength(), req.getUserMultiplier(), req.getQuantity());

        validator.validateItem(req, 1);

        if (req.getOrderType() == OrderType.RESHARPENING || req.getOrderType() == OrderType.REFORMING) {
            return priceServiceItem(req);
        }

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
        BigDecimal multipliedPrice = strategy.computeUnitPrice(enriched);

        if (multipliedPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new PricingException("Computed negative unit price");

        BigDecimal coatingCharge = resolveCoatingCharge(req, null);
        BigDecimal unitPrice = multipliedPrice.add(coatingCharge);

        BigDecimal lineSubtotal = PricingFormula.computeLineSubtotal(
                unitPrice, enriched.getQuantity());

        BigDecimal base = multipliedPrice.divide(enriched.getUserMultiplier(), 2, RoundingMode.HALF_UP);

        return PricingBreakdown.builder()
                .ratePerUnit(enriched.getRatePerUnit())
                .rateChartItem(enriched.getRateChartItem())
                .standardRodLengthMm(QuotationConstants.STANDARD_ROD_LENGTH_MM)
                .actualLengthMm(enriched.getOverallLength())
                .userMultiplier(enriched.getUserMultiplier())
                .basePrice(base)
                .multipliedPrice(multipliedPrice)
                .coatingCharge(coatingCharge)
                .unitPrice(unitPrice)
                .lineSubtotal(lineSubtotal)
                .rateSourceTable(rateChartClient.sourceTable())
                .build();
    }

    private PricingBreakdown priceServiceItem(QuotationItemRequest req) {
        if (req.getSpecs() == null) {
            throw new PricingException("specs required for RESHARPENING or REFORMING");
        }
        Double diameter = req.getSpecs().getDiameter();
        if (diameter == null) {
            throw new PricingException("specs.diameter required for service rate lookup");
        }

        String toolMaterial = (req.getSpecs().getMaterialType() != null && 
                req.getSpecs().getMaterialType().name().toUpperCase().contains("HSS")) ? "HSS" : "Carbide";

        ServiceType serviceType = req.getOrderType() == OrderType.RESHARPENING ? ServiceType.RE_SHARPENING : ServiceType.RE_FORMING;
        String toolType = req.getToolName();

        ToolServiceRateMaster rateRecord = toolServiceRateMasterService.findMatchingRate(serviceType, toolType, toolMaterial, diameter);
        if (rateRecord == null) {
            throw new PricingException("No matching rate found in service rate master for service=" + serviceType 
                    + ", tool=" + toolType + ", material=" + toolMaterial + ", diameter=" + diameter);
        }

        BigDecimal basePrice = rateRecord.getBaseRate();
        BigDecimal extraCharges = BigDecimal.ZERO;

        BigDecimal damageCharge = BigDecimal.ZERO;
        if (serviceType == ServiceType.RE_FORMING) {
            String damageLevel = req.getSpecs().getDamageLevel();
            if (damageLevel != null && !damageLevel.isBlank()) {
                damageCharge = switch (damageLevel.trim().toLowerCase()) {
                    case "minor" -> rateRecord.getMinorDamageCharge() != null ? rateRecord.getMinorDamageCharge() : BigDecimal.ZERO;
                    case "medium" -> rateRecord.getMediumDamageCharge() != null ? rateRecord.getMediumDamageCharge() : BigDecimal.ZERO;
                    case "major" -> rateRecord.getMajorDamageCharge() != null ? rateRecord.getMajorDamageCharge() : BigDecimal.ZERO;
                    default -> BigDecimal.ZERO;
                };
            }
            extraCharges = extraCharges.add(damageCharge);
        }

        if (serviceType == ServiceType.RE_SHARPENING && Boolean.TRUE.equals(req.getSpecs().getSpecialGeometry())) {
            BigDecimal geomCharge = rateRecord.getSpecialGeometryCharge() != null ? rateRecord.getSpecialGeometryCharge() : BigDecimal.ZERO;
            extraCharges = extraCharges.add(geomCharge);
        }

        if (serviceType == ServiceType.RE_FORMING && Boolean.TRUE.equals(req.getSpecs().getSpecialProfile())) {
            BigDecimal profCharge = rateRecord.getSpecialProfileCharge() != null ? rateRecord.getSpecialProfileCharge() : BigDecimal.ZERO;
            extraCharges = extraCharges.add(profCharge);
        }

        if (Boolean.TRUE.equals(req.getSpecs().getExpressDelivery())) {
            BigDecimal expCharge = rateRecord.getExpressDeliveryCharge() != null ? rateRecord.getExpressDeliveryCharge() : BigDecimal.ZERO;
            extraCharges = extraCharges.add(expCharge);
        }

        BigDecimal multiplier = req.getUserMultiplier() == null ? BigDecimal.ONE : req.getUserMultiplier();
        BigDecimal multipliedPrice = basePrice.add(extraCharges).multiply(multiplier);

        BigDecimal coatingCharge = resolveCoatingCharge(req, rateRecord);
        BigDecimal unitPrice = multipliedPrice.add(coatingCharge);

        BigDecimal lineSubtotal = PricingFormula.computeLineSubtotal(unitPrice, req.getQuantity());

        return PricingBreakdown.builder()
                .ratePerUnit(basePrice)
                .rateChartItem(rateRecord.getServiceCode())
                .standardRodLengthMm(0.0)
                .actualLengthMm(req.getOverallLength() != null ? req.getOverallLength() : 0.0)
                .userMultiplier(multiplier)
                .basePrice(basePrice)
                .multipliedPrice(multipliedPrice)
                .coatingCharge(coatingCharge)
                .unitPrice(unitPrice)
                .lineSubtotal(lineSubtotal)
                .rateSourceTable("tool_service_rate_masters")
                .rateRecordId(rateRecord.getId().toString())
                .build();
    }

    private BigDecimal resolveCoatingCharge(QuotationItemRequest req, ToolServiceRateMaster serviceRateRecord) {
        if (req.getSpecs() == null || !Boolean.TRUE.equals(req.getSpecs().getCoatingRequired()) || req.getSpecs().getCoatingType() == null) {
            return BigDecimal.ZERO;
        }

        String coating = req.getSpecs().getCoatingType().name();
        BigDecimal coatingCharge = BigDecimal.ZERO;

        // 1. Try to get it from the service rate record if provided
        if (serviceRateRecord != null) {
            BigDecimal rowCoatingCharge = null;
            if (coating.equalsIgnoreCase("TiN") || coating.equalsIgnoreCase("HELICA")) {
                rowCoatingCharge = serviceRateRecord.getCoatingTiN();
            } else if (coating.equalsIgnoreCase("TiAlN") || coating.equalsIgnoreCase("ALCRONA")) {
                rowCoatingCharge = serviceRateRecord.getCoatingTiAlN();
            } else if (coating.equalsIgnoreCase("AlCrN") || coating.equalsIgnoreCase("VICIOUS_BROWN")) {
                rowCoatingCharge = serviceRateRecord.getCoatingAlCrN();
            } else if (coating.equalsIgnoreCase("DLC") || coating.equalsIgnoreCase("VICIOUS_BLACK")) {
                rowCoatingCharge = serviceRateRecord.getCoatingDlc();
            }

            if (rowCoatingCharge != null && rowCoatingCharge.compareTo(BigDecimal.ZERO) > 0) {
                coatingCharge = rowCoatingCharge;
            }
        }

        // 2. Fall back to COATING_ONLY rate chart if not found/provided in service record
        if (coatingCharge.compareTo(BigDecimal.ZERO) <= 0) {
            Double diameter = req.getSpecs().getDiameter();
            if (diameter != null) {
                String toolMaterial = (req.getSpecs().getMaterialType() != null && 
                        req.getSpecs().getMaterialType().name().toUpperCase().contains("HSS")) ? "HSS" : "Carbide";
                String toolType = req.getToolName();

                ToolServiceRateMaster coatingRecord = toolServiceRateMasterService.findMatchingRate(
                        ServiceType.COATING_ONLY, toolType, toolMaterial, diameter);
                if (coatingRecord != null) {
                    if (coating.equalsIgnoreCase("TiN") || coating.equalsIgnoreCase("HELICA")) {
                        coatingCharge = coatingRecord.getCoatingTiN() != null ? coatingRecord.getCoatingTiN() : BigDecimal.ZERO;
                    } else if (coating.equalsIgnoreCase("TiAlN") || coating.equalsIgnoreCase("ALCRONA")) {
                        coatingCharge = coatingRecord.getCoatingTiAlN() != null ? coatingRecord.getCoatingTiAlN() : BigDecimal.ZERO;
                    } else if (coating.equalsIgnoreCase("AlCrN") || coating.equalsIgnoreCase("VICIOUS_BROWN")) {
                        coatingCharge = coatingRecord.getCoatingAlCrN() != null ? coatingRecord.getCoatingAlCrN() : BigDecimal.ZERO;
                    } else if (coating.equalsIgnoreCase("DLC") || coating.equalsIgnoreCase("VICIOUS_BLACK")) {
                        coatingCharge = coatingRecord.getCoatingDlc() != null ? coatingRecord.getCoatingDlc() : BigDecimal.ZERO;
                    }
                }
            }
        }

        // 3. Fall back to system default coating charge if enabled and still zero
        if (coatingCharge.compareTo(BigDecimal.ZERO) <= 0 && properties.isAutoApplyCoatingCharge()) {
            coatingCharge = properties.getDefaultCoatingCharge();
        }

        return coatingCharge;
    }

    @Override
    @Transactional
    public void recomputeQuotation(UUID quotationId) {
        log.info("Recomputing pricing for quotation {}", quotationId);
    }
}