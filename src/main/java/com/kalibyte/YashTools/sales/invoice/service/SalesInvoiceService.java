package com.kalibyte.YashTools.sales.invoice.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceRequest;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;

import java.util.UUID;

public interface SalesInvoiceService {
    SalesInvoiceResponse createInvoice(SalesInvoiceRequest request);
    SalesInvoiceResponse getInvoiceById(UUID id);
    SalesInvoiceResponse recordPayment(UUID id);
    PageResponse<SalesInvoiceResponse> getAllInvoices(int page, int size);
}
