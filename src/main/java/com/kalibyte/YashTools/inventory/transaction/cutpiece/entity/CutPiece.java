package com.kalibyte.YashTools.inventory.transaction.cutpiece.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.CutPieceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory_cut_pieces",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CutPiece extends AuditableEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id", nullable = false)
    private MaterialGrade materialGrade;

    @Column(name = "remaining_length", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingLength;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CutPieceStatus status;
}
