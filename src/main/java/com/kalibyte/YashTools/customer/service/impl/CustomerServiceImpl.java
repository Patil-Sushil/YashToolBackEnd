package com.kalibyte.YashTools.customer.service.impl;

import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.common.annotation.LoggableAction;
import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.customer.dto.CustomerRequest;
import com.kalibyte.YashTools.customer.dto.CustomerResponse;
import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.customer.mapper.CustomerMapper;
import com.kalibyte.YashTools.customer.repository.CustomerRepository;
import com.kalibyte.YashTools.customer.service.CustomerService;
import com.kalibyte.YashTools.company.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final com.kalibyte.YashTools.company.repository.CompanyRepository companyRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper, com.kalibyte.YashTools.company.repository.CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.companyRepository = companyRepository;
    }

    // Creates a new customer.
    // Mobile number must be unique among active (isDeleted=false) customers
    // Email must be unique among active (isDeleted=false) customers
    @Override
    @Transactional
    @LoggableAction(value = "Create Customer", action = AuditAction.CUSTOMER_CREATED, entityType = "CUSTOMER")
    public CustomerResponse createCustomer(CustomerRequest request) {

        // Check if mobile already exists for an active customer
        customerRepository.findByMobileNumberAndIsDeletedFalse(request.getMobileNumber())
                .ifPresent(c ->{throw new BusinessException("Mobile already exists");});
        // Check if email already exists for an active customer
        customerRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .ifPresent(c -> {throw new BusinessException("Email already exists");});

        // Build the Customer entity from request DTO using MapStruct
        Customer customer = customerMapper.toEntity(request);

        // Resolve company if companyCode is specified
        if (request.getCompanyCode() != null && !request.getCompanyCode().trim().isEmpty()) {
            Company company = companyRepository.findByCode(request.getCompanyCode().trim().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with code: " + request.getCompanyCode()));
            customer.setCompany(company);
        } else {
            // Fallback to active company context
            UUID activeCompanyId = com.kalibyte.YashTools.common.multi_company.CompanyContextHolder.getCompanyId();
            if (activeCompanyId != null) {
                Company company = companyRepository.findById(activeCompanyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Active company not found with ID: " + activeCompanyId));
                customer.setCompany(company);
            } else {
                // Fallback to default YT
                Company defaultCompany = companyRepository.findByCode("YT")
                        .orElseThrow(() -> new IllegalStateException("Default company (YT) not found"));
                customer.setCompany(defaultCompany);
            }
        }

        // Save the new customer and return the response DTO
        return customerMapper.toResponse(customerRepository.save(customer));
    }


    // Updates an existing customer (only if not deleted).
    @Override
    @Transactional
    @LoggableAction(value = "Update Customer", action = AuditAction.CUSTOMER_UPDATED, entityType = "CUSTOMER")
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
        // Fetch customer by ID and ensure it's not soft-deleted
        Customer customer = customerRepository.findById(customerId)
                .filter(c-> !c.getIsDeleted())
                .orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        // Update fields using MapStruct
        customerMapper.updateEntityFromRequest(request, customer);

        // Persist changes and return mapped response
        return customerMapper.toResponse(customerRepository.save(customer));
    }


    // Retrieves a customer by ID (only if not deleted).
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .filter(c-> !c.getIsDeleted())
                .orElseThrow(()-> new ResourceNotFoundException("customer not found"));
        return customerMapper.toResponse(customer);
    }

    // Retrieves all customers that are not soft-deleted.
    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAllByIsDeletedFalse()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAllCustomers(int page, int size) {
        Page<Customer> customers =
                customerRepository.findAllByIsDeletedFalse(org.springframework.data.domain.PageRequest.of(page, size));
        
        return PageResponse.from(customers, customerMapper::toResponse);
    }
}
