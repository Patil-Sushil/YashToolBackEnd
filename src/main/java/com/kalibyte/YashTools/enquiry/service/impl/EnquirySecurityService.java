package com.kalibyte.YashTools.enquiry.service.impl;
import com.kalibyte.YashTools.auth.security.token.CustomUserDetails;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security service for enquiry row-level access control
 *
 * <p>Implements custom authorization logic for enquiry access based on
 * user roles and ownership.</p>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Slf4j
@Service("enquirySecurityService")
@RequiredArgsConstructor
public class EnquirySecurityService {

    private final EnquiryRepository enquiryRepository;

    /**
     * Check if current user can access specific enquiry
     *
     * <p>Access rules:</p>
     * <ul>
     *   <li>ADMIN and SALES: Full access</li>
     *   <li>Other roles: Access only if they created the enquiry</li>
     * </ul>
     *
     * @param enquiryId the enquiry identifier
     * @return true if access is allowed
     */
    public boolean canAccessEnquiry(UUID enquiryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt to enquiry: {}", enquiryId);
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // ADMIN and SALES have full access
        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_SALES")) {
            log.debug("Admin/Sales access granted to enquiry: {}", enquiryId);
            return true;
        }

        // Check if user created the enquiry
        return enquiryRepository.findById(enquiryId)
                .map(enquiry -> {
                    boolean isOwner = enquiry.getCreatedBy().equals(username);
                    log.debug("User {} ownership check for enquiry {}: {}",
                            username, enquiryId, isOwner);
                    return isOwner;
                })
                .orElse(false);
    }

    /**
     * Check if authentication has specific role
     */
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}