package com.kalibyte.YashTools.common.multi_company;

import com.kalibyte.YashTools.auth.security.token.CustomUserDetails;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.company.repository.CompanyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CompanyContextFilter extends OncePerRequestFilter {

    private final CompanyRepository companyRepository;

    public CompanyContextFilter(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String headerCompanyIdStr = request.getHeader("X-Company-ID");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        UUID activeCompanyId = null;
        String activeCompanyCode = "YT";
        boolean isAllCompanies = false;

        if (headerCompanyIdStr != null && !headerCompanyIdStr.trim().isEmpty()) {
            if (headerCompanyIdStr.trim().equalsIgnoreCase("ALL")) {
                isAllCompanies = true;
                activeCompanyCode = "ALL";
            } else {
                try {
                    activeCompanyId = UUID.fromString(headerCompanyIdStr.trim());
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Company-ID format");
                    return;
                }
            }
        }

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            String userCompanyCode = userDetails.getCompanyCode();
            boolean hasCrossCompanyAccess = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN")
                            || role.equals("ROLE_SALES")
                            || role.equals("ROLE_MANAGER")
                            || role.equals("ROLE_FINANCE")
                            || role.equals("ROLE_CA"));

            if (isAllCompanies) {
                if (!hasCrossCompanyAccess) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to all companies is denied");
                    return;
                }
                // activeCompanyId remains null, activeCompanyCode is "ALL"
            } else if (activeCompanyId != null) {
                Company targetCompany = companyRepository.findById(activeCompanyId).orElse(null);
                if (targetCompany == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Company not found");
                    return;
                }
                if (!hasCrossCompanyAccess && userCompanyCode != null && !userCompanyCode.equalsIgnoreCase(targetCompany.getCode())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to company " + targetCompany.getCode() + " is denied");
                    return;
                }
                activeCompanyCode = targetCompany.getCode();
            } else {
                String codeToUse = (userCompanyCode != null) ? userCompanyCode : "YT";
                Company userCompany = companyRepository.findByCode(codeToUse).orElse(null);
                if (userCompany != null) {
                    activeCompanyId = userCompany.getId();
                    activeCompanyCode = userCompany.getCode();
                }
            }
        } else {
            if (isAllCompanies) {
                // activeCompanyId remains null, activeCompanyCode is "ALL"
            } else if (activeCompanyId != null) {
                Company company = companyRepository.findById(activeCompanyId).orElse(null);
                if (company != null) {
                    activeCompanyCode = company.getCode();
                }
            } else {
                Company defaultCompany = companyRepository.findByCode("YT").orElse(null);
                if (defaultCompany != null) {
                    activeCompanyId = defaultCompany.getId();
                    activeCompanyCode = defaultCompany.getCode();
                }
            }
        }

        if (activeCompanyId != null) {
            CompanyContextHolder.setCompanyId(activeCompanyId);
            CompanyContextHolder.setCompanyCode(activeCompanyCode);
        } else if (isAllCompanies) {
            CompanyContextHolder.setCompanyId(null);
            CompanyContextHolder.setCompanyCode("ALL");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContextHolder.clear();
        }
    }
}
