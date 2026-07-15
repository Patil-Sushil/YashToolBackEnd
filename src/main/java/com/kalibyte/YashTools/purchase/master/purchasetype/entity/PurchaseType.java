package com.kalibyte.YashTools.purchase.master.purchasetype.entity;

import com.kalibyte.YashTools.common.multi_company.BaseCompanyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(
        name = "purchase_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"})
        }
)
// @Filter(name = "companyFilter", condition = "company_id = :companyId")
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseType extends BaseCompanyEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
