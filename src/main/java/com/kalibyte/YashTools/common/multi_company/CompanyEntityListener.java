package com.kalibyte.YashTools.common.multi_company;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CompanyEntityListener {

    private static CompanyRepository companyRepository;

    @Autowired
    public void init(CompanyRepository companyRepository) {
        CompanyEntityListener.companyRepository = companyRepository;
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof CompanyAware companyAware) {
            if (companyAware.getCompany() == null) {
                UUID activeCompanyId = CompanyContextHolder.getCompanyId();
                if (activeCompanyId != null) {
                    Company company = companyRepository.findById(activeCompanyId)
                            .orElseThrow(() -> new IllegalArgumentException("Active company not found with ID: " + activeCompanyId));
                    companyAware.setCompany(company);
                } else {
                    // Fallback to default company (YT)
                    Company defaultCompany = companyRepository.findByCode("YT")
                            .orElseThrow(() -> new IllegalStateException("Default company (YT) not found in the database."));
                    companyAware.setCompany(defaultCompany);
                }
            }
        }
    }
}
