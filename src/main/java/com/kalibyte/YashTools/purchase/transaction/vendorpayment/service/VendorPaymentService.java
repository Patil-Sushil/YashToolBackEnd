package com.kalibyte.YashTools.purchase.transaction.vendorpayment.service;

import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentResponse;

import java.util.List;
import java.util.UUID;

public interface VendorPaymentService {
    VendorPaymentResponse create(VendorPaymentRequest request);
    VendorPaymentResponse getById(UUID id);
    List<VendorPaymentResponse> getAll();
    VendorPaymentResponse update(UUID id, VendorPaymentRequest request);
    void delete(UUID id);

    // Reports
    List<PurchaseInvoiceResponse> getOutstandingReport(UUID vendorId);
    List<VendorPaymentResponse> getPaymentHistoryReport(UUID vendorId);
    List<PurchaseInvoiceResponse> getOverdueInvoices();
}
