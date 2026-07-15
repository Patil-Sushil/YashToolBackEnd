package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response;

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
public class GoodsReceiptResponse {
    private UUID id;
    private String grnNumber;
    private LocalDate grnDate;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseOrderId;
    private String poNumber;
    private String warehouse;
    private String receivedBy;
    private String remarks;
    private List<GoodsReceiptItemResponse> items;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
