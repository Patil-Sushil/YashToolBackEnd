package com.kalibyte.YashTools.quotation.client;

import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionCoolantHoleRodPrice;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionCoolantHoleRodPriceRepository;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionRodNetPrice;
import com.kalibyte.YashTools.master.ratechart.repository.HyperionRodNetPriceRepository;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import com.kalibyte.YashTools.quotation.pricing.PricingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates "3.2X330MM UG CARBIDE ROD + grade PN90" into a numeric rate
 * by parsing the tool name and looking up the rate chart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateChartClient {

    private final HyperionRodNetPriceRepository rodRepo;
    private final HyperionCoolantHoleRodPriceRepository coolantRepo;

    private static final Pattern DIM_LEN =
            Pattern.compile("(\\d+\\.?\\d*)[xX](\\d+)\\s*MM", Pattern.CASE_INSENSITIVE);

    private boolean isCoolantHoleGrade(MaterialGrade g) {
        return g == MaterialGrade.TWO_THREE_HOLE_K40_330 || g == MaterialGrade.CENTRAL_PARALLEL_K40_330;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PricingContext enrichContext(PricingContext ctx) {
        if (ctx.getItemName() == null || ctx.getItemName().isBlank())
            throw new PricingException("Item name required for rate lookup");
        if (ctx.getMaterialGrade() == null)
            throw new PricingException("Material grade required for rate lookup");

        ParsedItem parsed = parseItemName(ctx.getItemName());
        log.debug("Parsed item: diameter={}, declared-length={}, item-key={}",
                parsed.diameter, parsed.declaredLength, parsed.itemKey);

        if (isCoolantHoleGrade(ctx.getMaterialGrade())) {
            String category = ctx.getMaterialGrade().getDescription();
            Optional<HyperionCoolantHoleRodPrice> rec =
                    coolantRepo.findByCategoryIgnoreCaseAndItemIgnoreCase(category, ctx.getItemName())
                            .or(() -> {
                                String normalized = ctx.getItemName().replaceAll("\\s+", " ").trim().toUpperCase();
                                return coolantRepo.findByCategoryIgnoreCaseAndItemIgnoreCase(category, normalized);
                            })
                            .or(() -> coolantRepo.findByCategoryIgnoreCaseAndItemIgnoreCase(category, parsed.itemKey))
                            .or(() -> coolantRepo.findByCategoryIgnoreCaseAndItemIgnoreCase(category, toFallback(ctx.getItemName())));

            if (rec.isEmpty())
                throw new PricingException("Rate not found for coolant hole rod item: " + ctx.getItemName() + " with grade: " + category);

            return PricingContext.builder()
                    .orderType(ctx.getOrderType())
                    .toolName(ctx.getToolName())
                    .itemName(ctx.getItemName())
                    .materialGrade(ctx.getMaterialGrade())
                    .quantity(ctx.getQuantity())
                    .trial(ctx.getTrial())
                    .overallLength(ctx.getOverallLength() != null ? ctx.getOverallLength() : parsed.declaredLength)
                    .userMultiplier(ctx.getUserMultiplier())
                    .coatingType(ctx.getCoatingType())
                    .ratePerUnit(rec.get().getPrice())
                    .rateChartItem(rec.get().getItem())
                    .interState(ctx.isInterState())
                    .build();
        }

        Optional<HyperionRodNetPrice> rec =
                rodRepo.findByItemIgnoreCaseAndActiveTrue(ctx.getItemName())
                        .or(() -> {
                            String normalized = ctx.getItemName().replaceAll("\\s+", " ").trim().toUpperCase();
                            return rodRepo.findByItemIgnoreCaseAndActiveTrue(normalized);
                        })
                        .or(() -> rodRepo.findByItemIgnoreCaseAndActiveTrue(parsed.itemKey))
                        .or(() -> rodRepo.findByItemIgnoreCaseAndActiveTrue(toFallback(ctx.getItemName())));

        if (rec.isEmpty())
            throw new PricingException("Rate not found for item: " + ctx.getItemName());

        BigDecimal rate = resolveGradeRate(rec.get(), ctx.getMaterialGrade());

        return PricingContext.builder()
                .orderType(ctx.getOrderType())
                .toolName(ctx.getToolName())
                .itemName(ctx.getItemName())
                .materialGrade(ctx.getMaterialGrade())
                .quantity(ctx.getQuantity())
                .trial(ctx.getTrial())
                .overallLength(ctx.getOverallLength() != null ? ctx.getOverallLength() : parsed.declaredLength)
                .userMultiplier(ctx.getUserMultiplier())
                .coatingType(ctx.getCoatingType())
                .ratePerUnit(rate)
                .rateChartItem(rec.get().getItem())
                .interState(ctx.isInterState())
                .build();
    }

    private BigDecimal resolveGradeRate(HyperionRodNetPrice r, MaterialGrade g) {
        return switch (g) {
            case  K40UF_H10F -> r.getK40ufH10f();
            case  AM70_DM80 -> r.getAm70Dm80();
            case  PN90 -> r.getPn90();
            case GP10_K10F -> r.getGp10K10f();
            default -> r.getPn90();
        };
    }

    private String toFallback(String tool) {
        return tool.replaceAll("(\\d+\\.?\\d*)[xX]\\d+\\s*MM", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase();
    }

    ParsedItem parseItemName(String tool) {
        Matcher m = DIM_LEN.matcher(tool);
        Double diameter = null;
        Double length = null;
        String key = tool;
        if (m.find()) {
            try {
                diameter = Double.parseDouble(m.group(1));
                length = Double.parseDouble(m.group(2));
                key = m.replaceAll("").trim();
            } catch (NumberFormatException ignored) { }
        }
        key = key.replaceAll("\\s+", " ").trim().toUpperCase();
        return new ParsedItem(diameter, length, key);
    }

    public String sourceTable() {
        return "hyperion_rod_net_price";
    }

    record ParsedItem(Double diameter, Double declaredLength, String itemKey) { }
}