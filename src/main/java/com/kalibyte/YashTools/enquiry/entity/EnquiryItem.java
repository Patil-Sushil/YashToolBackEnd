package com.kalibyte.YashTools.enquiry.entity;

import com.kalibyte.YashTools.common.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Individual line item within an enquiry
 *
 * <p>Each item represents a specific tool order with type-specific specifications.
 * Uses polymorphic association for different spec types based on order type.</p>
 *
 * <p><b>Specification Rules:</b></p>
 * <ul>
 *   <li>NEW_TOOL → requires NewToolSpecs</li>
 *   <li>RESHARPENING → requires ResharpeningSpecs</li>
 *   <li>REFORMING → requires ReformingSpecs</li>
 * </ul>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Entity
@Table(
        name = "enquiry_items",
        indexes = {
                @Index(name = "idx_item_enquiry", columnList = "enquiry_id"),
                @Index(name = "idx_item_order_type", columnList = "order_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Parent enquiry reference
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    /**
     * Type of order (NEW_TOOL, RESHARPENING, REFORMING)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    /**
     * Tool product name/identifier
     */
    @Column(name = "tool_name", nullable = false, length = 200)
    private String toolName;

    /**
     * Quantity ordered (default: 1 for trial items)
     * <p>For trial orders: max = 1</p>
     * <p>For regular orders: min = 1, no max limit</p>
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Trial flag
     * <p>When true, quantity must be exactly 1</p>
     * <p>User can manually set quantity, but validation enforces max = 1</p>
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean trial = false;

    /**
     * Item-specific remarks or special instructions
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * Reference to technical drawing or specification document
     */
    @Column(name = "drawing_reference", length = 500)
    private String drawingReference;

    // ========================================
    // Polymorphic Specifications
    // ========================================

    /**
     * Specifications for NEW_TOOL orders
     * Null for other order types
     */
    @OneToOne(
            mappedBy = "enquiryItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private NewToolSpecs newToolSpecs;

    /**
     * Specifications for REFORMING orders
     * Null for other order types
     */
    @OneToOne(
            mappedBy = "enquiryItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private ReformingSpecs reformingSpecs;

    /**
     * Specifications for RESHARPENING orders
     * Null for other order types
     */
    @OneToOne(
            mappedBy = "enquiryItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private ResharpeningSpecs resharpeningSpecs;

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Set NEW_TOOL specifications and maintain bidirectional relationship
     *
     * @param specs the new tool specifications
     */
    public void setNewToolSpecs(NewToolSpecs specs) {
        if (specs == null) {
            if (this.newToolSpecs != null) {
                this.newToolSpecs.setEnquiryItem(null);
            }
        } else {
            specs.setEnquiryItem(this);
        }
        this.newToolSpecs = specs;
    }

    /**
     * Set REFORMING specifications and maintain bidirectional relationship
     *
     * @param specs the reforming specifications
     */
    public void setReformingSpecs(ReformingSpecs specs) {
        if (specs == null) {
            if (this.reformingSpecs != null) {
                this.reformingSpecs.setEnquiryItem(null);
            }
        } else {
            specs.setEnquiryItem(this);
        }
        this.reformingSpecs = specs;
    }

    /**
     * Set RESHARPENING specifications and maintain bidirectional relationship
     *
     * @param specs the resharpening specifications
     */
    public void setResharpeningSpecs(ResharpeningSpecs specs) {
        if (specs == null) {
            if (this.resharpeningSpecs != null) {
                this.resharpeningSpecs.setEnquiryItem(null);
            }
        } else {
            specs.setEnquiryItem(this);
        }
        this.resharpeningSpecs = specs;
    }

    /**
     * Check if this is a trial order
     *
     * @return true if trial flag is set
     */
    public boolean isTrial() {
        return Boolean.TRUE.equals(trial);
    }
}