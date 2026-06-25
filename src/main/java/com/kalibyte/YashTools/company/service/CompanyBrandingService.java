package com.kalibyte.YashTools.company.service;

import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyBrandingService {

    private final CompanyRepository companyRepository;

    public Company getBrandingForCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
    }
}
