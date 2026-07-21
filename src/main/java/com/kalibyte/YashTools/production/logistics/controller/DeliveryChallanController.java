package com.kalibyte.YashTools.production.logistics.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.email.dto.EmailResult;
import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanRequest;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryChallanResponse;
import com.kalibyte.YashTools.production.logistics.dto.DeliveryReceiptRequest;
import com.kalibyte.YashTools.production.logistics.pdf.service.DeliveryChallanPdfService;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanEmailService;
import com.kalibyte.YashTools.production.logistics.service.DeliveryChallanService;
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
@RequestMapping("/api/delivery-challans")
@RequiredArgsConstructor
public class DeliveryChallanController {

    private final DeliveryChallanService service;
    private final DeliveryChallanPdfService pdfService;
    private final DeliveryChallanEmailService emailService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> createChallan(@Valid @RequestBody DeliveryChallanRequest request) {
        DeliveryChallanResponse response = service.createChallan(request);
        return ResponseEntity.ok(ApiResponse.success("Delivery Challan created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> getChallanById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getChallanById(id)));
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> dispatchChallan(@PathVariable UUID id) {
        DeliveryChallanResponse response = service.dispatchChallan(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery Challan dispatched successfully", response));
    }

    @PostMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<DeliveryChallanResponse>> recordReceipt(@PathVariable UUID id, @Valid @RequestBody DeliveryReceiptRequest request) {
        DeliveryChallanResponse response = service.recordDeliveryReceipt(id, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery Receipt recorded successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryChallanResponse>>> listChallans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Delivery Challans retrieved successfully", service.getAllChallans(page, size)));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        DeliveryChallanResponse challan = service.getChallanById(id);
        byte[] pdf = pdfService.generatePdf(challan);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "DeliveryChallan-" + challan.getChallanNo() + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, 200);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<EmailResult>> sendToCustomer(
            @PathVariable UUID id,
            @RequestParam(required = false) List<String> cc) {
        return ResponseEntity.ok(ApiResponse.success("Delivery Challan email dispatched",
                (cc == null || cc.isEmpty())
                        ? emailService.sendChallan(id)
                        : emailService.sendChallan(id, cc)));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<EmailResult>> resend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Delivery Challan email resent",
                emailService.resend(id)));
    }

    @GetMapping("/{id}/email-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE', 'PRODUCTION')")
    public ResponseEntity<ApiResponse<List<EmailLog>>> emailHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getDeliveryHistory(id)));
    }
}
