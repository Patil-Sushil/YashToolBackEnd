package com.example.YashToolBackEnd.customer.controller;

import com.example.YashToolBackEnd.customer.dto.CustomerRequest;
import com.example.YashToolBackEnd.customer.dto.CustomerResponse;
import com.example.YashToolBackEnd.customer.service.CustomerService;
import com.example.YashToolBackEnd.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Create new customer
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Customer created successfully", customerService.createCustomer(request)));
    }

    // Update an existing customer by id
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable("customerId") Long customerId,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully",
                customerService.updateCustomer(customerId, request)));
    }

    // Get a single customer by id
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer retrieved successfully",
                customerService.getCustomerById(customerId)));
    }

    // Get all customers
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers retrieved successfully",
                customerService.getAllCustomers()));
    }
}