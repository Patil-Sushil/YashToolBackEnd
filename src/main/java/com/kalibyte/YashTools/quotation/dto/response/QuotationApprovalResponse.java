package com.kalibyte.YashTools.quotation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationApprovalResponse {
    private UUID id;
    private UUID quotationId;
    private String approvalStatus;
    private BigDecimal currentDiscountPercentage;
    private BigDecimal requestedDiscountPercentage;
    private BigDecimal requestedDiscountAmount;
    private BigDecimal approvalThreshold;
    private Boolean exceedsThreshold;
    private String requestedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    private String approvedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String approverComments;
}