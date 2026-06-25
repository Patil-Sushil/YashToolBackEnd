package com.kalibyte.YashTools.common.util;

import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

// Generates human readable unique numbers for various entities
// Examples:
// *  YT-ENQ-2025-0001
// *  YT-QT-2025-0001
// *  YT-WO-2025-0001
public final class NumberGeneratorUtil {
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private static final DateTimeFormatter YEAR_FORMAT =
            DateTimeFormatter.ofPattern("yyyy");

    private NumberGeneratorUtil(){
        // prevent instantiation
    }

    public static String generate(String typePrefix) {
        String companyPrefix = CompanyContextHolder.getCompanyCode();
        if (companyPrefix == null || companyPrefix.trim().isEmpty()) {
            companyPrefix = "YT";
        }
        int next = SEQUENCE.incrementAndGet();
        String year = LocalDate.now().format(YEAR_FORMAT);
        return String.format("%s-%s-%s-%04d", companyPrefix, typePrefix, year, next);
    }

    /**
     * Resets sequence (ONLY for tests)
     */
    static void resetForTest() {
        SEQUENCE.set(0);
    }
}
