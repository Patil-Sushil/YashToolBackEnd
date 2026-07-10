package com.kalibyte.YashTools.quotation.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotation_items", indexes = {
        @Index(name = "idx_qitem_quotation", columnList = "quotation_id"),
        @Index(name = "idx_qitem_line", columnList = "quotation_id, line_number"),
        @Index(name = "idx_qitem_material_grade", columnList = "material_grade")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(callSuper = true)
public class QuotationItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(name = "tool_name", nullable = false, length = 200)
    private String toolName;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean trial = false;

    @Column(name = "item_remarks", columnDefinition = "TEXT")
    private String itemRemarks;

    @Column(name = "drawing_reference", length = 500)
    private String drawingReference;

    @Column(name = "material_type", length = 30)
    private String materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_grade", length = 30)
    private MaterialGrade materialGrade;

    @Column(name = "coating_required", nullable = false)
    @Builder.Default
    private Boolean coatingRequired = false;

    @Column(name = "coating_type", length = 30)
    private String coatingType;

    @Column(name = "resharpening_type", length = 20)
    private String resharpeningType;

    @Column private Double diameter;
    @Column(name = "flute_length") private Double fluteLength;
    @Column(name = "shank_diameter") private Double shankDiameter;
    @Column(name = "overall_length") private Double overallLength;
    @Column(name = "technical_notes", columnDefinition = "TEXT")
    private String technicalNotes;

    @Column(name = "damage_level", length = 50)
    private String damageLevel;

    @Column(name = "special_geometry")
    @Builder.Default
    private Boolean specialGeometry = false;

    @Column(name = "special_profile")
    @Builder.Default
    private Boolean specialProfile = false;

    @Column(name = "express_delivery")
    @Builder.Default
    private Boolean expressDelivery = false;

    @Column(name = "rate_chart_item")
    private String rateChartItem;
    @Column(name = "rate_chart_grade", length = 30)
    private String rateChartGrade;
    @Column(name = "rate_per_unit", nullable = false)
    private BigDecimal ratePerUnit;

    @Column(name = "standard_rod_length", nullable = false)
    @Builder.Default
    private BigDecimal standardRodLength = BigDecimal.valueOf(330.0);

    @Column(name = "actual_length_used", nullable = false)
    private BigDecimal actualLengthUsed;
    @Column(name = "user_multiplier", nullable = false)
    private BigDecimal userMultiplier;
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    @Column(name = "multiplied_price", nullable = false)
    private BigDecimal multipliedPrice;

    @Column(name = "coating_charge", nullable = false)
    @Builder.Default
    private BigDecimal coatingCharge = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    @Column(name = "line_subtotal", nullable = false)
    private BigDecimal lineSubtotal;

    @Column(name = "line_discount_amount", nullable = false)
    @Builder.Default
    private BigDecimal lineDiscountAmount = BigDecimal.ZERO;

    @Column(name = "line_taxable_amount", nullable = false)
    private BigDecimal lineTaxableAmount;

    @Column(name = "line_cgst", nullable = false)
    @Builder.Default
    private BigDecimal lineCgst = BigDecimal.ZERO;
    @Column(name = "line_sgst", nullable = false)
    @Builder.Default
    private BigDecimal lineSgst = BigDecimal.ZERO;
    @Column(name = "line_igst", nullable = false)
    @Builder.Default
    private BigDecimal lineIgst = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
    @Column(name = "hsn_sac_code", length = 10)
    private String hsnSacCode;
    @Column(name = "rate_source_table")
    private String rateSourceTable;
    @Column(name = "rate_record_id")
    private String rateRecordId;
    @Column(name = "rate_fetched_at", nullable = false)
    private LocalDateTime rateFetchedAt;
}