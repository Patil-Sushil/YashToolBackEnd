package com.kalibyte.YashTools.quotation.service.impl;

import com.kalibyte.YashTools.quotation.config.QuotationProperties;
import com.kalibyte.YashTools.quotation.dto.request.ApproveRejectDiscountRequest;
import com.kalibyte.YashTools.quotation.dto.response.QuotationApprovalResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationApproval;
import com.kalibyte.YashTools.quotation.entity.enums.ApprovalStatus;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import com.kalibyte.YashTools.quotation.exception.QuotationNotFoundException;
import com.kalibyte.YashTools.quotation.exception.QuotationStateException;
import com.kalibyte.YashTools.quotation.mapper.QuotationMapper;
import com.kalibyte.YashTools.quotation.repository.QuotationApprovalRepository;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.quotation.security.QuotationSecurityService;
import com.kalibyte.YashTools.quotation.service.QuotationApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationApprovalServiceImpl implements QuotationApprovalService {

    private final QuotationApprovalRepository approvalRepository;
    private final QuotationRepository quotationRepository;
    private final QuotationMapper mapper;
    private final QuotationSecurityService security;
    private final QuotationProperties props;

    @Override
    @Transactional
    public QuotationApprovalResponse requestApproval(UUID id, BigDecimal requestedDiscount) {
        Quotation q = security.loadForCurrentCompany(id);
        BigDecimal threshold = BigDecimal.valueOf(props.getApprovalThresholdPercentage());
        BigDecimal amount = q.getSubtotal().multiply(requestedDiscount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        QuotationApproval a = QuotationApproval.builder()
                .quotation(q)
                .requestId(UUID.randomUUID())
                .requestedBy(security.currentUsername())
                .requestedAt(LocalDateTime.now())
                .currentDiscountPercentage(q.getDiscountPercentage())
                .requestedDiscountPercentage(requestedDiscount)
                .requestedDiscountAmount(amount)
                .approvalStatus(ApprovalStatus.PENDING)
                .approvalThreshold(threshold)
                .exceedsThreshold(requestedDiscount.compareTo(threshold) > 0)
                .build();
        q.addApproval(a);
        approvalRepository.save(a);

        log.info("Approval requested for {}: {}% (threshold {}%)",
                q.getQuotationNo(), requestedDiscount, threshold);
        return toResponse(a);
    }

    @Override
    @Transactional
    public QuotationResponse processApproval(ApproveRejectDiscountRequest req) {
        if (!security.isAdmin())
            throw new QuotationStateException("Only ADMIN can process approvals");

        QuotationApproval a = approvalRepository.findById(req.getApprovalId())
                .orElseThrow(() -> new QuotationNotFoundException(
                        "Approval: " + req.getApprovalId()));
        if (!a.getApprovalStatus().isPending())
            throw new QuotationStateException("Already processed: " + a.getApprovalStatus());

        Quotation q = a.getQuotation();

        if ("APPROVED".equalsIgnoreCase(req.getDecision())) {
            a.setApprovalStatus(ApprovalStatus.APPROVED);
            a.setApprovedBy(security.currentUsername());
            a.setApprovedAt(LocalDateTime.now());
            a.setApproverComments(req.getComments());
            q.setDiscountPercentage(a.getRequestedDiscountPercentage());
            q.setDiscountAmount(a.getRequestedDiscountAmount());
            q.setStatus(QuotationStatus.APPROVED);
        } else if ("REJECTED".equalsIgnoreCase(req.getDecision())) {
            a.setApprovalStatus(ApprovalStatus.REJECTED);
            a.setApprovedBy(security.currentUsername());
            a.setApprovedAt(LocalDateTime.now());
            a.setRejectionReason(req.getComments());
            q.setStatus(QuotationStatus.REJECTED);
        } else {
            throw new QuotationStateException("Invalid decision: " + req.getDecision());
        }

        approvalRepository.save(a);
        return mapper.toResponse(quotationRepository.save(q));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationApprovalResponse> getPendingApprovals() {
        if (!security.isAdmin()) {
            return List.of();
        }
        return approvalRepository.findByApprovalStatus(ApprovalStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public boolean requiresApproval(BigDecimal pct) {
        return pct.compareTo(BigDecimal.valueOf(props.getApprovalThresholdPercentage())) > 0;
    }

    private QuotationApprovalResponse toResponse(QuotationApproval a) {
        return QuotationApprovalResponse.builder()
                .id(a.getId())
                .quotationId(a.getQuotation().getId())
                .approvalStatus(a.getApprovalStatus().name())
                .currentDiscountPercentage(a.getCurrentDiscountPercentage())
                .requestedDiscountPercentage(a.getRequestedDiscountPercentage())
                .requestedDiscountAmount(a.getRequestedDiscountAmount())
                .approvalThreshold(a.getApprovalThreshold())
                .exceedsThreshold(a.getExceedsThreshold())
                .requestedBy(a.getRequestedBy())
                .requestedAt(a.getRequestedAt())
                .approvedBy(a.getApprovedBy())
                .approvedAt(a.getApprovedAt())
                .rejectionReason(a.getRejectionReason())
                .approverComments(a.getApproverComments())
                .build();
    }
}