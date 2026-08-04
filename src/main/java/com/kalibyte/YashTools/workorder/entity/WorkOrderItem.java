
package com.kalibyte.YashTools.workorder.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.common.enums.OrderType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import com.kalibyte.YashTools.workorder.entity.enums.TrialStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "work_order_items", indexes = {
        @Index(name = "idx_woitem_work_order", columnList = "work_order_id"),
        @Index(name = "idx_woitem_quotation_item", columnList = "quotation_item_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkOrderItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_item_id")
    private QuotationItem quotationItem;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "trial_status", length = 30)
    private TrialStatus trialStatus;

    @Column(name = "trial_feedback", columnDefinition = "TEXT")
    private String trialFeedback;

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
}
