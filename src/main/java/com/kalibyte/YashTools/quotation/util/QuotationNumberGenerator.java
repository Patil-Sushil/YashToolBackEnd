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

    /**
     * Extracts the base quotation number without revision suffix e.g., "YT-QT-2026-000001"
     */
    public static String getBaseQuotationNo(String quotationNo) {
        if (quotationNo == null) return null;
        int rIndex = quotationNo.lastIndexOf("-R");
        if (rIndex > 0) {
            String suffix = quotationNo.substring(rIndex + 2);
            if (suffix.matches("\\d+")) {
                return quotationNo.substring(0, rIndex);
            }
        }
        return quotationNo;
    }

    /**
     * Builds revision quotation number with suffix e.g., "YT-QT-2026-000001-R1"
     */
    public static String buildRevisionNumber(String baseQuotationNo, int revisionNumber) {
        String cleanBase = getBaseQuotationNo(baseQuotationNo);
        if (revisionNumber <= 0) {
            return cleanBase;
        }
        return cleanBase + "-R" + revisionNumber;
    }
}