package com.example.YashToolBackEnd.common.util;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

// Generates human readable unique numbers for various entities
//Examples:
// *  ENQ-2025-0001
// *  QT-2025-0001
// *  WO-2025-0001
public final class NumberGeneratorUtil {
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private static final DateTimeFormatter YEAR_FORMAT =
            DateTimeFormatter.ofPattern("yyyy");

    private NumberGeneratorUtil(){
        // prevent instantiation
    }

    public static String generate(String prefix) {

        int next = SEQUENCE.incrementAndGet();

        String year = LocalDate.now().format(YEAR_FORMAT);

        return String.format("%s-%s-%04d", prefix, year, next);


    }

    /**
     * Resets sequence (ONLY for tests)
     */
    static void resetForTest() {
        SEQUENCE.set(0);
    }
}
