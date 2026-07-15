package com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response;

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
public class VendorPaymentResponse {
    private UUID id;
    private String paymentNumber;
    private LocalDate paymentDate;
    private UUID vendorId;
    private String vendorName;
    private String paymentMethod;
    private String paymentReferenceNumber;
    private String remarks;
    private java.math.BigDecimal totalPayment;
    private List<VendorPaymentItemResponse> items;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
