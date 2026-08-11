package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.quotation.util.QuotationNumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuotationNumberingServiceTest {

    @Test
    @DisplayName("Initial quotation number should not have revision suffix")
    void testInitialQuotationNumberFormat() {
        String quotationNo = QuotationNumberGenerator.build(1);
        assertTrue(quotationNo.startsWith("YT-QT-"));
        assertFalse(quotationNo.contains("-R"));
        assertFalse(quotationNo.contains(")R"));
        assertEquals(quotationNo, QuotationNumberGenerator.getBaseQuotationNo(quotationNo));
    }

    @Test
    @DisplayName("First revision should have -R1 suffix")
    void testFirstRevisionNumberFormat() {
        String baseQuotationNo = "YT-QT-2026-000001";
        String revision1 = QuotationNumberGenerator.buildRevisionNumber(baseQuotationNo, 1);
        assertEquals("YT-QT-2026-000001-R1", revision1);
        assertEquals("YT-QT-2026-000001", QuotationNumberGenerator.getBaseQuotationNo(revision1));
    }

    @Test
    @DisplayName("Subsequent revisions should increment revision suffix correctly")
    void testSubsequentRevisionNumberFormat() {
        String baseQuotationNo = "YT-QT-2026-000001";
        String revision2 = QuotationNumberGenerator.buildRevisionNumber(baseQuotationNo, 2);
        assertEquals("YT-QT-2026-000001-R2", revision2);
        assertEquals("YT-QT-2026-000001", QuotationNumberGenerator.getBaseQuotationNo(revision2));

        String revisionFromRevision = QuotationNumberGenerator.buildRevisionNumber("YT-QT-2026-000001-R1", 3);
        assertEquals("YT-QT-2026-000001-R3", revisionFromRevision);
    }
}
