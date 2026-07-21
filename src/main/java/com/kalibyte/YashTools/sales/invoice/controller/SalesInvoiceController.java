package com.kalibyte.YashTools.sales.invoice.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceRequest;
import com.kalibyte.YashTools.sales.invoice.dto.SalesInvoiceResponse;
import com.kalibyte.YashTools.sales.invoice.pdf.service.SalesInvoicePdfService;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceEmailService;
import com.kalibyte.YashTools.sales.invoice.service.SalesInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales-invoices")
@RequiredArgsConstructor
public class SalesInvoiceController {

    private final SalesInvoiceService service;
    private final SalesInvoicePdfService pdfService;
    private final SalesInvoiceEmailService emailService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SalesInvoiceResponse>> createInvoice(@Valid @RequestBody SalesInvoiceRequest request) {
        SalesInvoiceResponse response = service.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.success("Sales Invoice created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SalesInvoiceResponse>> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getInvoiceById(id)));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SalesInvoiceResponse>> recordPayment(@PathVariable UUID id) {
        SalesInvoiceResponse response = service.recordPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<PageResponse<SalesInvoiceResponse>>> listInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Sales Invoices retrieved successfully", service.getAllInvoices(page, size)));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        SalesInvoiceResponse invoice = service.getInvoiceById(id);
        byte[] pdf = pdfService.generatePdf(invoice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SalesInvoice-" + invoice.getInvoiceNo() + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, 200);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<EmailResult>> sendToCustomer(
            @PathVariable UUID id,
            @RequestParam(required = false) List<String> cc) {
        return ResponseEntity.ok(ApiResponse.success("Sales Invoice email dispatched",
                (cc == null || cc.isEmpty())
                        ? emailService.sendInvoice(id)
                        : emailService.sendInvoice(id, cc)));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<EmailResult>> resend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Sales Invoice email resent",
                emailService.resend(id)));
    }

    @GetMapping("/{id}/email-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<EmailLog>>> emailHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getDeliveryHistory(id)));
    }
}
