package com.kalibyte.YashTools.inventory.master.category.entity;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "inventory_categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"}),
                @UniqueConstraint(columnNames = {"name"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends AuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
