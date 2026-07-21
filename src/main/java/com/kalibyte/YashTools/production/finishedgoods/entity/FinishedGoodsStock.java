package com.kalibyte.YashTools.production.finishedgoods.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "finished_goods_stocks")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinishedGoodsStock extends BaseCompanyEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_item_id", nullable = false, unique = true)
    private WorkOrderItem workOrderItem;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;
}
