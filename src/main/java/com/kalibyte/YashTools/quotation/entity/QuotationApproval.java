package com.kalibyte.YashTools.quotation.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.quotation.entity.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotation_approvals", indexes = {
        @Index(name = "idx_approval_quotation", columnList = "quotation_id"),
        @Index(name = "idx_approval_status", columnList = "approval_status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuotationApproval extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "current_discount_percentage", nullable = false)
    private BigDecimal currentDiscountPercentage;

    @Column(name = "requested_discount_percentage", nullable = false)
    private BigDecimal requestedDiscountPercentage;

    @Column(name = "requested_discount_amount", nullable = false)
    private BigDecimal requestedDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private String approvedBy;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    @Column(name = "approver_comments", columnDefinition = "TEXT")
    private String approverComments;

    @Column(name = "approval_threshold", nullable = false)
    private BigDecimal approvalThreshold;

    @Column(name = "exceeds_threshold", nullable = false)
    private Boolean exceedsThreshold;
}