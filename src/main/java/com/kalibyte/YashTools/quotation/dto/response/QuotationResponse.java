package com.kalibyte.YashTools.quotation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationResponse {

    private UUID quotationId;
    private String quotationNo;
    private Integer version;
    private UUID parentQuotationId;
    private String parentQuotationNo;
    private UUID rootQuotationId;
    private String rootQuotationNo;
    private Boolean isRevision;
    private Integer revisionNumber;
    private UUID sourceEnquiryId;
    private String sourceType;
    private String status;
    private Boolean isLocked;

    private UUID customerId;
    private String customerCompanyName;
    private String customerContactPerson;
    private String customerEmail;
    private String customerMobile;

    private Boolean isUrgent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;
    private Integer revisionCount;
    private Boolean revisionRequired;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal taxableAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private String currency;

    private String remarks;
    private String internalNotes;
    private String termsAndConditions;
    private String paymentTerms;
    private String deliveryTerms;

    private String customerDecision;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime customerDecisionAt;
    private String customerDecisionRemarks;

    private List<QuotationItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private String updatedBy;

    private String companyCode;
    private UUID companyId;
}