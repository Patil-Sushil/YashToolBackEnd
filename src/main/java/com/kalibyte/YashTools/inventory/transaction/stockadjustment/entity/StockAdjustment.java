package com.kalibyte.YashTools.inventory.transaction.stockadjustment.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentReason;
import com.kalibyte.YashTools.inventory.shared.enums.StockAdjustmentType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory_stock_adjustments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"adjustment_number"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustment extends AuditableEntity {

    @Column(name = "adjustment_number", nullable = false)
    private String adjustmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id")
    private MaterialGrade materialGrade;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    private StockAdjustmentType adjustmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private StockAdjustmentReason reason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
