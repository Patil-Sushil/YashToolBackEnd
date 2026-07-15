package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_goods_receipt_items")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"goodsReceipt"})
@ToString(exclude = {"goodsReceipt"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_reference_id", nullable = false)
    private PurchaseOrderItem poItemReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id")
    private MaterialGrade materialGrade;

    @Column(name = "ordered_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal orderedQuantity;

    @Builder.Default
    @Column(name = "previously_received_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal previouslyReceivedQuantity = BigDecimal.ZERO;

    @Column(name = "current_received_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentReceivedQuantity;

    @Column(name = "accepted_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal acceptedQuantity;

    @Builder.Default
    @Column(name = "rejected_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "pending_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal pendingQuantity;
}
