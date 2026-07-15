package com.kalibyte.YashTools.inventory.transaction.stocktake.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.shared.enums.StockTakeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "inventory_stock_takes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"stock_take_number"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTake extends AuditableEntity {

    @Column(name = "stock_take_number", nullable = false)
    private String stockTakeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StockTakeStatus status;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "stockTake", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockTakeLine> lines = new ArrayList<>();
}
