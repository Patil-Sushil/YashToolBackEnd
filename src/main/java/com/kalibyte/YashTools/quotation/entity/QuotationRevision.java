package com.kalibyte.YashTools.quotation.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quotation_revisions", indexes = {
        @Index(name = "idx_revision_quotation", columnList = "quotation_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuotationRevision extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "revision_type", nullable = false, length = 30)
    private String revisionType;

    @Column(name = "revision_reason", columnDefinition = "TEXT", nullable = false)
    private String revisionReason;

    @Column(name = "previous_subtotal")
    private BigDecimal previousSubtotal;
    @Column(name = "previous_discount")
    private BigDecimal previousDiscount;
    @Column(name = "previous_grand_total")
    private BigDecimal previousGrandTotal;
    @Column(name = "new_subtotal")
    private BigDecimal newSubtotal;
    @Column(name = "new_discount")
    private BigDecimal newDiscount;
    @Column(name = "new_grand_total")
    private BigDecimal newGrandTotal;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;
    @Column(name = "new_status", length = 30)
    private String newStatus;

    @Column(name = "discount_change_amount")
    private BigDecimal discountChangeAmount;
    @Column(name = "new_discount_percentage")
    private BigDecimal newDiscountPercentage;
}