package com.kalibyte.YashTools.production.packing.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "packing_logs")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PackingLog extends BaseCompanyEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_item_id", nullable = false)
    private WorkOrderItem workOrderItem;

    @Column(name = "packing_no", nullable = false, length = 50, unique = true)
    private String packingNo;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    @Column(name = "package_size", nullable = false, length = 50)
    private String packageSize;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "container_status", nullable = false, length = 50)
    @Builder.Default
    private String containerStatus = "PACKED";

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
