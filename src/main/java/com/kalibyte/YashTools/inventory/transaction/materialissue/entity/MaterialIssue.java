package com.kalibyte.YashTools.inventory.transaction.materialissue.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueStatus;
import com.kalibyte.YashTools.inventory.shared.enums.MaterialIssueType;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory_material_issues",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"issue_number"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialIssue extends AuditableEntity {

    @Column(name = "issue_number", nullable = false)
    private String issueNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_grade_id", nullable = false)
    private MaterialGrade materialGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private MaterialIssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cut_piece_id")
    private CutPiece cutPiece;

    @Column(name = "required_length", nullable = false, precision = 19, scale = 4)
    private BigDecimal requiredLength;

    @Column(name = "issued_length", nullable = false, precision = 19, scale = 4)
    private BigDecimal issuedLength;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_cut_piece_id")
    private CutPiece newCutPiece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_card_id")
    private com.kalibyte.YashTools.production.jobcard.entity.JobCard jobCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MaterialIssueStatus status;
}
