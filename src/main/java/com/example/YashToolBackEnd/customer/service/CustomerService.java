package com.example.YashToolBackEnd.customer.service;

import com.example.YashToolBackEnd.customer.dto.CustomerRequest;
import com.example.YashToolBackEnd.customer.dto.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest customerRequest);

    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);

    CustomerResponse getCustomerById(UUID customerId );

    List<CustomerResponse> getAllCustomers();
}
