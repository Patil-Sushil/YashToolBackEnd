package com.kalibyte.YashTools.quotation.security;

import com.kalibyte.YashTools.auth.security.token.CustomUserDetails;
import com.kalibyte.YashTools.common.exception.UnauthorizedException;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.exception.QuotationNotFoundException;
import com.kalibyte.YashTools.quotation.repository.QuotationRepository;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotationSecurityService {

    private final QuotationRepository quotationRepository;

    public Quotation loadForCurrentCompany(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new UnauthorizedException("Not authenticated");
        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails cud))
            throw new UnauthorizedException("Invalid principal");

        return quotationRepository.findByIdAndCompanyId(id, CompanyContextHolder.getCompanyId())
                .orElseThrow(() -> new QuotationNotFoundException(id.toString()));
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "SYSTEM" : auth.getName();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}