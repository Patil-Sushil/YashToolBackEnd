package com.kalibyte.YashTools.common.multi_company;

import com.kalibyte.YashTools.common.base.AuditableEntity;
import com.kalibyte.YashTools.company.entity.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(CompanyEntityListener.class)
public abstract class BaseCompanyEntity extends AuditableEntity implements CompanyAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
