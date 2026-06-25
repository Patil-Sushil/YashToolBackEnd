package com.kalibyte.YashTools.company.controller;

import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.company.dto.CompanyResponse;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyResponse> response = companies.stream()
                .map(c -> CompanyResponse.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .name(c.getName())
                        .gstNumber(c.getGstNumber())
                        .bankName(c.getBankName())
                        .bankAccountNo(c.getBankAccountNo())
                        .bankIfsc(c.getBankIfsc())
                        .bankBranch(c.getBankBranch())
                        .address(c.getAddress())
                        .logoUrl(c.getLogoUrl())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Companies retrieved successfully", response));
    }
}
