package com.kalibyte.YashTools.production.logistics.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_challan_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryChallanItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_challan_id", nullable = false)
    private DeliveryChallan deliveryChallan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_item_id", nullable = false)
    private WorkOrderItem workOrderItem;

    @Column(nullable = false)
    private Integer quantity;
}
