package com.kalibyte.YashTools.quotation.util;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class QuotationNumberGenerator {

    private static final DateTimeFormatter YF = DateTimeFormatter.ofPattern("yyyy");

    private QuotationNumberGenerator() {}

    public static String buildPrefix() {
        String code = CompanyContextHolder.getCompanyCode();
        if (code == null || code.isBlank()) code = "YT";
        return String.format("%s-%s-%s-", code,
                QuotationConstants.QUOTATION_NUMBER_PREFIX,
                LocalDate.now().format(YF));
    }

    public static String formatSequence(long seq) {
        return String.format("%0" + QuotationConstants.QUOTATION_NUMBER_SEQUENCE_LENGTH + "d", seq);
    }

    public static String build(long yearSequence) {
        return buildPrefix() + formatSequence(yearSequence);
    }
}