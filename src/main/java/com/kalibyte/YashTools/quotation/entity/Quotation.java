package com.kalibyte.YashTools.quotation.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations", indexes = {
        @Index(name = "idx_quotation_no", columnList = "quotation_no"),
        @Index(name = "idx_quotation_status", columnList = "status"),
        @Index(name = "idx_quotation_customer", columnList = "customer_id"),
        @Index(name = "idx_quotation_company", columnList = "company_id"),
        @Index(name = "idx_quotation_enquiry", columnList = "source_enquiry_id"),
        @Index(name = "idx_quotation_parent", columnList = "parent_quotation_id"),
        @Index(name = "idx_quotation_validity", columnList = "valid_until")
})
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quotation extends BaseCompanyEntity {

    @Column(name = "quotation_no", nullable = false, length = 30, unique = true)
    private String quotationNo;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_quotation_id")
    private Quotation parentQuotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_enquiry_id")
    private Enquiry sourceEnquiry;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "is_urgent", nullable = false)
    @Builder.Default
    private Boolean isUrgent = false;
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "revision_required", nullable = false)
    @Builder.Default
    private Boolean revisionRequired = false;
    @Column(name = "revision_count", nullable = false)
    @Builder.Default
    private Integer revisionCount = 0;
    @Column(name = "last_revision_at")
    private LocalDateTime lastRevisionAt;

    @Column(name = "customer_company_name")
    private String customerCompanyName;
    @Column(name = "customer_contact_person")
    private String customerContactPerson;
    @Column(name = "customer_email")
    private String customerEmail;
    @Column(name = "customer_mobile")
    private String customerMobile;

    @Column(name = "subtotal", nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "discount_percentage", nullable = false)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    @Column(name = "taxable_amount", nullable = false)
    @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;
    @Column(name = "cgst_amount", nullable = false)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;
    @Column(name = "sgst_amount", nullable = false)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;
    @Column(name = "igst_amount", nullable = false)
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;
    @Column(name = "total_tax", nullable = false)
    @Builder.Default
    private BigDecimal totalTax = BigDecimal.ZERO;
    @Column(name = "grand_total", nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(columnDefinition = "TEXT")
    private String remarks;
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    @Column(name = "payment_terms", columnDefinition = "TEXT")
    private String paymentTerms;
    @Column(name = "delivery_terms", columnDefinition = "TEXT")
    private String deliveryTerms;

    @Column(name = "customer_decision", length = 20)
    private String customerDecision;
    @Column(name = "customer_decision_at")
    private LocalDateTime customerDecisionAt;
    @Column(name = "customer_decision_remarks", columnDefinition = "TEXT")
    private String customerDecisionRemarks;

    @Column(name = "sent_to_customer_at")
    private LocalDateTime sentToCustomerAt;
    @Column(name = "sent_to_customer_by")
    private String sentToCustomerBy;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuotationRevision> revisions = new ArrayList<>();

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuotationApproval> approvals = new ArrayList<>();

    // ============== helpers ==============

    public void addItem(QuotationItem item) {
        items.add(item);
        item.setQuotation(this);
    }

    public void removeItem(QuotationItem item) {
        items.remove(item);
        item.setQuotation(null);
    }

    public void addRevision(QuotationRevision r) {
        revisions.add(r);
        r.setQuotation(this);
    }

    public void addApproval(QuotationApproval a) {
        approvals.add(a);
        a.setQuotation(this);
    }

    public boolean isEditable() {
        return QuotationStatus.EDITABLE.contains(status) && !Boolean.TRUE.equals(isLocked);
    }

    public boolean isTerminal() {
        return QuotationStatus.TERMINAL.contains(status);
    }
}