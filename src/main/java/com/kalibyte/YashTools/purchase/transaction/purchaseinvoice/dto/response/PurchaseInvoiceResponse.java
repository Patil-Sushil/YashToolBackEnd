package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private String supplierInvoiceNumber;
    private LocalDate invoiceDate;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseOrderId;
    private String poNumber;
    private List<UUID> goodsReceiptIds;
    private List<String> goodsReceiptNumbers;
    private String gstDetails;
    private BigDecimal freight;
    private BigDecimal otherCharges;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String paymentStatus;
    private String status;
    private List<PurchaseInvoiceItemResponse> items;
    private List<String> warnings;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
