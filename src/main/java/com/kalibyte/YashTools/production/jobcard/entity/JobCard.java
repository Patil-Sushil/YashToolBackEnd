package com.kalibyte.YashTools.production.jobcard.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import com.kalibyte.YashTools.production.jobcard.entity.enums.JobCardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "job_cards")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobCard extends BaseCompanyEntity {

    @Column(name = "job_card_no", nullable = false, length = 30, unique = true)
    private String jobCardNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_item_id", nullable = false)
    private WorkOrderItem workOrderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private JobCardStatus status = JobCardStatus.CREATED;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 1;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_rework", nullable = false)
    @Builder.Default
    private Boolean isRework = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rework_parent_job_card_id")
    private JobCard reworkParentJobCard;
}
