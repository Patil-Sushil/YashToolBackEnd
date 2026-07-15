package com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPaymentItemResponse {
    private UUID id;
    private UUID purchaseInvoiceId;
    private String invoiceNumber;
    private BigDecimal invoiceAmount;
    private BigDecimal outstandingBeforePayment;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAfterPayment;
}
