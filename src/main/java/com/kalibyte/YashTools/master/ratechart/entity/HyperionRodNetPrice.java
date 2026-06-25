package com.kalibyte.YashTools.master.ratechart.entity;

import com.kalibyte.YashTools.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "hyperion_rod_net_price",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hyperion_rod_net_price_item",
            columnNames = {"item"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HyperionRodNetPrice extends BaseEntity {

    @Column(nullable = false)
    private String item;

    @Column(name = "k40uf_h10f", nullable = false)
    private BigDecimal k40ufH10f;

    @Column(name = "am70_dm80", nullable = false)
    private BigDecimal am70Dm80;

    @Column(name = "pn90", nullable = false)
    private BigDecimal pn90;

    @Column(name = "gp10_k10f", nullable = false)
    private BigDecimal gp10K10f;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}