package com.kalibyte.YashTools.quotation.util;

import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import com.kalibyte.YashTools.quotation.exception.PricingException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PricingFormula {

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private PricingFormula() {}

    /** base = (rate / 330) × length */
    public static BigDecimal computeBasePrice(BigDecimal rate, BigDecimal length) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0)
            throw new PricingException("Rate must be positive: " + rate);
        if (length == null || length.compareTo(BigDecimal.ZERO) <= 0)
            throw new PricingException("Length must be positive: " + length);

        BigDecimal perMm = rate.divide(
                BigDecimal.valueOf(QuotationConstants.STANDARD_ROD_LENGTH_MM), 10, RM);
        return perMm.multiply(length).setScale(SCALE, RM);
    }

    public static BigDecimal computeMultipliedPrice(BigDecimal base, BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0)
            throw new PricingException("Multiplier must be positive");
        if (multiplier.compareTo(BigDecimal.valueOf(QuotationConstants.MAX_MULTIPLIER)) > 0)
            throw new PricingException("Multiplier exceeds max " + QuotationConstants.MAX_MULTIPLIER);

        return base.multiply(multiplier).setScale(SCALE, RM);
    }

    public static BigDecimal computeUnitPrice(BigDecimal multiplied, BigDecimal coatingCharge) {
        BigDecimal coating = coatingCharge == null ? BigDecimal.ZERO : coatingCharge;
        BigDecimal total = multiplied.add(coating);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            throw new PricingException("Computed negative unit price");
        return total.setScale(SCALE, RM);
    }

    public static BigDecimal computeLineSubtotal(BigDecimal unitPrice, int quantity) {
        if (quantity <= 0) throw new PricingException("Quantity must be positive");
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, RM);
    }

    public static BigDecimal computeTax(BigDecimal taxable, BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO.setScale(SCALE);
        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), 6, RM);
        return taxable.multiply(factor).setScale(SCALE, RM);
    }

    public static BigDecimal applyDiscount(BigDecimal subtotal, BigDecimal discountAmount) {
        BigDecimal d = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal result = subtotal.subtract(d);
        if (result.compareTo(BigDecimal.ZERO) < 0) result = BigDecimal.ZERO;
        return result.setScale(SCALE, RM);
    }
}