package com.kalibyte.YashTools.production.tracking.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import com.kalibyte.YashTools.production.tracking.entity.enums.InspectionResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_inspections")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QualityInspection extends BaseCompanyEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_card_id", nullable = false, unique = true)
    private JobCard jobCard;

    @Column(name = "accepted_quantity", nullable = false)
    private Integer acceptedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    @Builder.Default
    private Integer rejectedQuantity = 0;

    @Column(name = "rework_quantity", nullable = false)
    @Builder.Default
    private Integer reworkQuantity = 0;

    @Column(nullable = false, length = 255)
    private String inspector;

    @Column(name = "inspection_date", nullable = false)
    @Builder.Default
    private LocalDateTime inspectionDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionResult result;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
