package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.quotation.dto.request.ApproveRejectDiscountRequest;
import com.kalibyte.YashTools.quotation.dto.response.QuotationApprovalResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.enums.ApprovalStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface QuotationApprovalService {
    QuotationApprovalResponse requestApproval(UUID quotationId, BigDecimal requestedDiscount);
    QuotationResponse processApproval(ApproveRejectDiscountRequest request);
    List<QuotationApprovalResponse> getPendingApprovals();
    boolean requiresApproval(BigDecimal discountPercentage);
}