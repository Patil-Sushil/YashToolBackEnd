package com.kalibyte.YashTools.sales.invoice.email;

import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;

import java.util.List;
import java.util.UUID;

public interface SalesInvoiceEmailHandler {
    UUID sendInvoiceEmail(SalesInvoiceResponse invoice);
    UUID sendInvoiceEmail(SalesInvoiceResponse invoice, byte[] pdfBytes);
    UUID sendInvoiceEmail(SalesInvoiceResponse invoice, byte[] pdfBytes, List<String> ccList);
}
