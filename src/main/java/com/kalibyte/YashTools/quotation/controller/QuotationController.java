package com.kalibyte.YashTools.quotation.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.quotation.dto.request.*;
import com.kalibyte.YashTools.quotation.dto.response.*;
import com.kalibyte.YashTools.quotation.pdf.service.QuotationPdfService;
import com.kalibyte.YashTools.quotation.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
public class QuotationController {

    private final QuotationService quotationService;
    private final QuotationRevisionService revisionService;
    private final QuotationApprovalService approvalService;
    private final QuotationPdfService pdfService;
    private final QuotationEmailService quotationEmailService;

    // ============================================
    // CREATE
    // ============================================
    @PostMapping("/from-enquiry")
    public ResponseEntity<ApiResponse<QuotationResponse>> createFromEnquiry(
            @Valid @RequestBody CreateFromEnquiryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Quotation created from enquiry",
                quotationService.createFromEnquiry(request)));
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<QuotationResponse>> createDirect(
            @Valid @RequestBody CreateDirectQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Direct quotation created",
                quotationService.createDirect(request)));
    }

    // ============================================
    // APPROVALS
    // ============================================
    @GetMapping("/approvals/pending")
    public ResponseEntity<ApiResponse<List<QuotationApprovalResponse>>> pendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success(approvalService.getPendingApprovals()));
    }

    @PostMapping("/approvals/process")
    public ResponseEntity<ApiResponse<QuotationResponse>> processApproval(
            @Valid @RequestBody ApproveRejectDiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Approval processed",
                approvalService.processApproval(request)));
    }

    // ============================================
    // READ
    // ============================================
    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getById(id)));
    }

    @GetMapping("/by-number/{quotationNo}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getByNumber(
            @PathVariable String quotationNo) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getByNumber(quotationNo)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuotationResponse> result = quotationService.listForCurrentCompany(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(result)));
    }

    // ============================================
    // UPDATE
    // ============================================
    @PutMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateDraft(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Quotation updated",
                quotationService.updateDraft(id, request)));
    }

    // ============================================
    // REVISIONS
    // ============================================
    @PostMapping("/revisions")
    public ResponseEntity<ApiResponse<QuotationResponse>> revise(
            @Valid @RequestBody ReviseQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Quotation revised",
                quotationService.revise(request)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/revisions")
    public ResponseEntity<ApiResponse<List<QuotationRevisionResponse>>> revisions(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(revisionService.getRevisionHistory(id)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/revised-quotations")
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getRevisedQuotations(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Revised quotations fetched",
                revisionService.getRevisedQuotations(id)));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/revision-chain")
    public ResponseEntity<ApiResponse<QuotationFamilyResponse>> getRevisionChain(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation revision chain fetched",
                revisionService.getRevisionChain(id)));
    }

    @GetMapping("/by-number/{quotationNo}/revised-quotations")
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getRevisedQuotationsByNumber(
            @PathVariable String quotationNo) {
        return ResponseEntity.ok(ApiResponse.success("Revised quotations fetched",
                revisionService.getRevisedQuotationsByNumber(quotationNo)));
    }

    @GetMapping("/by-number/{quotationNo}/revision-chain")
    public ResponseEntity<ApiResponse<QuotationFamilyResponse>> getRevisionChainByNumber(
            @PathVariable String quotationNo) {
        return ResponseEntity.ok(ApiResponse.success("Quotation revision chain fetched",
                revisionService.getRevisionChainByNumber(quotationNo)));
    }

    // ============================================
    // SEND / RESEND
    // ============================================
    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<EmailResult>> sendToCustomer(
            @PathVariable UUID id,
            @RequestParam(required = false) List<String> cc) {
        EmailResult result = (cc == null || cc.isEmpty())
                ? quotationEmailService.sendQuotation(id)
                : quotationEmailService.sendQuotation(id, cc);
        if (!result.isSuccess()) {
            String msg = result.getProviderResponseMessage() != null ? result.getProviderResponseMessage() : "Email delivery failed";
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Quotation email dispatch failed: " + msg, result));
        }
        return ResponseEntity.ok(ApiResponse.success("Quotation email dispatched successfully", result));
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<ApiResponse<EmailResult>> resend(@PathVariable UUID id) {
        EmailResult result = quotationEmailService.resend(id);
        if (!result.isSuccess()) {
            String msg = result.getProviderResponseMessage() != null ? result.getProviderResponseMessage() : "Email delivery failed";
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Quotation email resend failed: " + msg, result));
        }
        return ResponseEntity.ok(ApiResponse.success("Quotation email resent successfully", result));
    }

    // ============================================
    // LOCK / CANCEL
    // ============================================
    @PostMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<QuotationResponse>> lockFinal(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation locked",
                quotationService.lockFinal(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<QuotationResponse>> cancel(
            @PathVariable UUID id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("Quotation cancelled",
                quotationService.cancel(id, reason)));
    }

    @PostMapping("/{id}/customer-decision")
    public ResponseEntity<ApiResponse<QuotationResponse>> recordCustomerDecision(
            @PathVariable UUID id,
            @RequestParam String decision,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(ApiResponse.success("Customer decision recorded",
                quotationService.recordCustomerDecision(id, decision, remarks)));
    }

    @PostMapping("/{id}/admin-approve")
    public ResponseEntity<ApiResponse<QuotationResponse>> adminApprove(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation approved by admin",
                quotationService.adminApprove(id)));
    }

    @PostMapping("/{id}/admin-reject")
    public ResponseEntity<ApiResponse<QuotationResponse>> adminReject(
            @PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Quotation rejected by admin",
                quotationService.adminReject(id, reason)));
    }

    // ============================================
    // PDF
    // ============================================
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        QuotationResponse q = quotationService.getById(id);
        byte[] pdf = pdfService.generatePdf(q);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "Quotation-" + q.getQuotationNo() + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, 200);
    }

    // ============================================
    // EMAIL HISTORY
    // ============================================
    @GetMapping("/{id}/email-history")
    public ResponseEntity<ApiResponse<List<EmailLog>>> emailHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                quotationEmailService.getDeliveryHistory(id)));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> searchQuotations(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuotationResponse> result = quotationService.searchQuotations(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Quotations retrieved successfully", PageResponse.from(result)));
    }

}