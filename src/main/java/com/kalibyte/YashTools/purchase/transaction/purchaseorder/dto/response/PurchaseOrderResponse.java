package com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response;

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
public class PurchaseOrderResponse {
    private UUID id;
    private String poNumber;
    private LocalDate poDate;
    private UUID vendorId;
    private String vendorName;
    private LocalDate expectedDeliveryDate;
    private UUID paymentTermsId;
    private String paymentTermsCode;
    private String paymentTermsName;
    private UUID purchaseTypeId;
    private String purchaseTypeCode;
    private String purchaseTypeName;
    private String status;
    private String remarks;
    private List<PurchaseOrderItemResponse> items;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
