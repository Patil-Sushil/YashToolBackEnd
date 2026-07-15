package com.kalibyte.YashTools.inventory.transaction.stocktake.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_stock_take_lines")
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTakeLine extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_take_id", nullable = false)
    private StockTake stockTake;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id")
    private MaterialGrade materialGrade;

    @Column(name = "system_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    @Column(name = "physical_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal physicalQuantity;

    @Column(name = "difference_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal differenceQuantity;

    @Builder.Default
    @Column(name = "approved", nullable = false)
    private Boolean approved = false;
}
