package com.kalibyte.YashTools.customer.controller;

import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.response.ApiResponse;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.customer.dto.CustomerRequest;
import com.kalibyte.YashTools.customer.dto.CustomerResponse;
import com.kalibyte.YashTools.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Create new customer
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    @LoggableAction(value = "Create Customer", action = AuditAction.CUSTOMER_CREATED, entityType = "CUSTOMER")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    // Update an existing customer by id
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    @LoggableAction(value = "Update Customer", action = AuditAction.CUSTOMER_UPDATED, entityType = "CUSTOMER")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    // Get a single customer by id
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES') or @securityService.canAccessCustomer(#customerId)")
    @LoggableAction(value = "Retrieve Customer By ID", action = AuditAction.CUSTOMER_VIEWED, entityType = "CUSTOMER")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @PathVariable("customerId") UUID customerId) {
        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", response));
    }

    // Get all customers (paginated)
    @GetMapping
    @LoggableAction(value = "Retrieve All Customers", action = AuditAction.CUSTOMER_VIEWED, entityType = "CUSTOMER")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CustomerResponse> customers = customerService.getAllCustomers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    @LoggableAction(value = "Search Customers", action = AuditAction.CUSTOMER_VIEWED, entityType = "CUSTOMER")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> searchCustomers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CustomerResponse> result = customerService.searchCustomers(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", result));
    }

}
