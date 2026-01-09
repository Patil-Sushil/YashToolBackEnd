package com.example.YashToolBackEnd.customer.service;

import com.example.YashToolBackEnd.customer.dto.CustomerRequest;
import com.example.YashToolBackEnd.customer.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest customerRequest);

    CustomerResponse updateCustomer(Long customerId, CustomerRequest request);

    CustomerResponse getCustomerById(Long customerId );

    List<CustomerResponse> getAllCustomers();
}
