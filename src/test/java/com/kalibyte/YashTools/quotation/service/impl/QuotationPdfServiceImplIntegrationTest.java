package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.pdf.service.QuotationPdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class QuotationPdfServiceImplIntegrationTest {

    @Autowired
    private QuotationPdfService pdfService;

    @Test
    void testPdfGeneration() throws Exception {
        QuotationItemResponse item = QuotationItemResponse.builder()
                .lineNumber(1)
                .itemName("3.2X330MM UG CARBIDE ROD")
                .toolName("Solid Carbide Endmill 10mm")
                .overallLength(100.0)
                .materialType("CARBIDE")
                .materialGrade("K40UF_H10F")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(836))
                .lineSubtotal(BigDecimal.valueOf(836))
                .lineDiscountAmount(BigDecimal.ZERO)
                .lineTotal(BigDecimal.valueOf(836))
                .rateChartItem("3.2X330MM UG CARBIDE ROD")
                .build();

        QuotationResponse q = QuotationResponse.builder()
                .quotationId(UUID.randomUUID())
                .quotationNo("QT-2026-000001")
                .companyId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")) // Swara Enterprises
                .companyCode("SW")
                .customerCompanyName("YT Auto Components")
                .customerEmail("customer@example.com")
                .createdAt(LocalDateTime.now())
                .items(List.of(item))
                .subtotal(BigDecimal.valueOf(836))
                .grandTotal(BigDecimal.valueOf(836))
                .build();

        byte[] pdf = pdfService.generatePdf(q);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);

        com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdf);
        assertTrue(reader.getNumberOfPages() > 0);

        try (FileOutputStream fos = new FileOutputStream("test_quotation.pdf")) {
            fos.write(pdf);
        }
    }
}
