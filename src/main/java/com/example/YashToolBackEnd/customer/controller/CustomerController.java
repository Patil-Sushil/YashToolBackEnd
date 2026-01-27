package com.example.YashToolBackEnd.customer.controller;

import com.example.YashToolBackEnd.customer.dto.CustomerRequest;
import com.example.YashToolBackEnd.customer.dto.CustomerResponse;
import com.example.YashToolBackEnd.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Create new customer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    // Update an existing customer by id
    @PatchMapping("/{customerId}")
    public CustomerResponse updateCustomer(
            @PathVariable("customerId") Long customerId,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(customerId, request);
    }

    // Get a single customer by id
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(
            @PathVariable("customerId") Long customerId) {
        return customerService.getCustomerById(customerId);
    }

    // Get all customers
    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }
}