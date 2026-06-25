package com.kalibyte.YashTools.enquiry.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Root aggregate for customer enquiries
 *
 * <p>Represents a customer's request for quotation containing one or more items.
 * Each enquiry is uniquely identified by a system-generated enquiry number.</p>
 *
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>Must contain at least one enquiry item</li>
 *   <li>Trial items allowed for all order types (max qty: 1)</li>
 *   <li>Enquiry number format: ENQ-YYYY-####</li>
 * </ul>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Entity
@Table(
        name = "enquiries",
        indexes = {
                @Index(name = "idx_enquiry_no", columnList = "enquiry_no"),
                @Index(name = "idx_enquiry_status", columnList = "status"),
                @Index(name = "idx_enquiry_customer", columnList = "customer_id"),
                @Index(name = "idx_enquiry_created", columnList = "created_at")
        }
)
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enquiry extends BaseCompanyEntity {

    /**
     * System-generated unique enquiry identifier
     * Format: ENQ-YYYY-####
     * Example: ENQ-2024-0001
     */
    @Column(name = "enquiry_no", nullable = false, length = 20)
    private String enquiryNo;

    /**
     * Associated customer entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Priority flag for urgent enquiries requiring expedited processing
     */
    @Column(name = "is_urgent", nullable = false)
    @Builder.Default
    private Boolean isUrgent = false;

    /**
     * Current processing status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnquiryStatus status = EnquiryStatus.CREATED;

    /**
     * Enquiry-level items (one-to-many relationship)
     * Cascade all operations to maintain aggregate consistency
     */
    @OneToMany(
            mappedBy = "enquiry",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<EnquiryItem> items = new ArrayList<>();

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Add item to enquiry and maintain bidirectional relationship
     *
     * @param item the enquiry item to add
     */
    public void addItem(EnquiryItem item) {
        items.add(item);
        item.setEnquiry(this);
    }

    /**
     * Remove item from enquiry
     *
     * @param item the enquiry item to remove
     */
    public void removeItem(EnquiryItem item) {
        items.remove(item);
        item.setEnquiry(null);
    }

    /**
     * Check if enquiry contains trial items
     *
     * @return true if any item is marked as trial
     */
    public boolean hasTrial() {
        return items.stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getTrial()));
    }

    /**
     * Check if enquiry is in editable state
     *
     * @return true if status allows modification
     */
    public boolean isEditable() {
        return status == EnquiryStatus.CREATED ||
                status == EnquiryStatus.UNDER_REVIEW;
    }

    /**
     * Get total item count
     *
     * @return number of items in enquiry
     */
    public int getItemCount() {
        return items.size();
    }
}