package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import com.kalibyte.YashTools.email.service.EmailRetryService;
import com.kalibyte.YashTools.email.service.EmailService;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.email.QuotationEmailHandler;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.pdf.service.QuotationPdfService;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.QuotationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationEmailServiceTest {

    @Mock
    private QuotationService quotationService;
    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private QuotationSecurityService security;
    @Mock
    private QuotationEmailHandler handler;
    @Mock
    private QuotationPdfService pdfService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailRetryService emailRetryService;

    @InjectMocks
    private QuotationEmailServiceImpl quotationEmailService;

    private UUID quotationId;
    private Quotation quotation;
    private QuotationResponse responseDto;

    @BeforeEach
    void setUp() {
        quotationId = UUID.randomUUID();
        quotation = Quotation.builder()
                .status(QuotationStatus.APPROVED)
                .build();
        quotation.setId(quotationId);

        responseDto = QuotationResponse.builder()
                .quotationId(quotationId)
                .quotationNo("YT-QT-2026-000001")
                .customerEmail("customer@example.com")
                .build();
    }

    @Test
    @DisplayName("Should not mark quotation as sent if email dispatch fails")
    void testSendQuotation_EmailFailed_ShouldNotMarkAsSent() {
        when(quotationRepository.findById(quotationId)).thenReturn(Optional.of(quotation));
        when(quotationService.getById(quotationId)).thenReturn(responseDto);
        when(pdfService.generatePdf(responseDto)).thenReturn(new byte[]{1, 2, 3});

        EmailResult failedResult = EmailResult.failed(UUID.randomUUID(), "Authentication failed");
        when(handler.sendQuotationEmail(eq(responseDto), any(), anyList())).thenReturn(failedResult);

        EmailResult result = quotationEmailService.sendQuotation(quotationId);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(EmailStatus.FAILED, result.getStatus());
        assertEquals("Authentication failed", result.getProviderResponseMessage());
        assertEquals(QuotationStatus.APPROVED, quotation.getStatus());
        verify(quotationRepository, never()).save(any(Quotation.class));
    }

    @Test
    @DisplayName("Should mark quotation as sent if email dispatch succeeds")
    void testSendQuotation_EmailSuccess_ShouldMarkAsSent() {
        when(quotationRepository.findById(quotationId)).thenReturn(Optional.of(quotation));
        when(quotationService.getById(quotationId)).thenReturn(responseDto);
        when(pdfService.generatePdf(responseDto)).thenReturn(new byte[]{1, 2, 3});

        EmailResult successResult = EmailResult.ok(UUID.randomUUID(), EmailStatus.SENT, "<msg-1>", "250", "Delivered");
        when(handler.sendQuotationEmail(eq(responseDto), any(), anyList())).thenReturn(successResult);
        when(security.currentUsername()).thenReturn("admin@toolserp.com");

        EmailResult result = quotationEmailService.sendQuotation(quotationId);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(EmailStatus.SENT, result.getStatus());
        assertEquals(QuotationStatus.SENT_TO_CUSTOMER, quotation.getStatus());
        verify(quotationRepository, times(1)).save(quotation);
    }
}
