package com.kalibyte.YashTools.production.logistics.damage.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallan;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageAction;
import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageStatus;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transit_damage_reports")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitDamageReport extends BaseCompanyEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_challan_id", nullable = false)
    private DeliveryChallan deliveryChallan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_item_id", nullable = false)
    private WorkOrderItem workOrderItem;

    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransitDamageAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TransitDamageStatus status = TransitDamageStatus.REPORTED;

    @Column(name = "reported_by", length = 255)
    private String reportedBy;

    @Column(name = "reported_at", nullable = false)
    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_job_card_id")
    private JobCard createdJobCard;
}
