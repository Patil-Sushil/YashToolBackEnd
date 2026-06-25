package com.kalibyte.YashTools.master.ratechart.entity;

import com.kalibyte.YashTools.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "hyperion_coolant_hole_rod_price",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_coolant_hole_category_item",
            columnNames = {"category", "item"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HyperionCoolantHoleRodPrice extends BaseEntity {

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String item;

    @Column(nullable = false)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}