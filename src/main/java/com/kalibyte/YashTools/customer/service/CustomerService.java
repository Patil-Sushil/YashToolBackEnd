package com.kalibyte.YashTools.customer.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.customer.dto.CustomerRequest;
import com.kalibyte.YashTools.customer.dto.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest customerRequest);

    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);

    CustomerResponse getCustomerById(UUID customerId );

    List<CustomerResponse> getAllCustomers();

    PageResponse<CustomerResponse> getAllCustomers(int page, int size);
}
