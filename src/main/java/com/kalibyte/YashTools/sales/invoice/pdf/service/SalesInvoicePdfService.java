package com.kalibyte.YashTools.sales.invoice.pdf.service;

import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;

import java.util.UUID;

public interface SalesInvoicePdfService {
    byte[] generatePdf(UUID id);
    byte[] generatePdf(SalesInvoiceResponse invoice);
}
