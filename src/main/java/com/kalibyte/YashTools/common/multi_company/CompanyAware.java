package com.kalibyte.YashTools.common.multi_company;

import com.kalibyte.YashTools.company.entity.Company;

public interface CompanyAware {
    Company getCompany();
    void setCompany(Company company);
}
