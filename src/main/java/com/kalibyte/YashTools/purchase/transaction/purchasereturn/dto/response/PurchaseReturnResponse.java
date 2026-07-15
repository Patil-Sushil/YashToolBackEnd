package com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnResponse {
    private UUID id;
    private String returnNumber;
    private LocalDate returnDate;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseInvoiceId;
    private String purchaseInvoiceNumber;
    private UUID goodsReceiptId;
    private String grnNumber;
    private UUID purchaseOrderId;
    private String poNumber;
    private String remarks;
    private List<PurchaseReturnItemResponse> items;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
