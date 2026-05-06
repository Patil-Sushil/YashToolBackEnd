package com.kalibyte.YashTools.common.util;

import com.kalibyte.YashTools.customer.repository.CustomerRepository;
import com.kalibyte.YashTools.enquiry.repository.EnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final EnquiryRepository enquiryRepository;
    private final CustomerRepository customerRepository;

    /**
     * Checks if the current user can access/modify an enquiry.
     * ADMIN and SALES can access any enquiry.
     * Other roles (if any added later) would be restricted to their own.
     */
    public boolean canAccessEnquiry(UUID enquiryId) {
        // Admin and Sales have full access to enquiries
        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_SALES")) {
            return true;
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        return enquiryRepository.findById(enquiryId)
                .map(enquiry -> currentUsername.equals(enquiry.getCreatedBy()))
                .orElse(false);
    }

    /**
     * Checks if the current user can access/modify a customer.
     * ADMIN and SALES can access any customer.
     */
    public boolean canAccessCustomer(UUID customerId) {
        // Admin and Sales have full access to customers
        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_SALES")) {
            return true;
        }

        String currentUsername = SecurityUtils.getCurrentUsername();
        return customerRepository.findById(customerId)
                .map(customer -> currentUsername.equals(customer.getCreatedBy()))
                .orElse(false);
    }

    private boolean hasRole(String role) {
        return SecurityUtils.getCurrentUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
