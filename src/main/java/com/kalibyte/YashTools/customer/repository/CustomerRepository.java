package com.kalibyte.YashTools.customer.repository;

import com.kalibyte.YashTools.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByMobileNumberAndIsDeletedFalse(String mobileNumber);

    Optional<Customer> findByEmailAndIsDeletedFalse(String email);

    List<Customer> findAllByIsDeletedFalse();

    org.springframework.data.domain.Page<Customer> findAllByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);


    @org.springframework.data.jpa.repository.Query("SELECT c FROM Customer c WHERE c.isDeleted = false AND (" +
            "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Customer> searchCustomers(
            @org.springframework.data.repository.query.Param("search") String search,
            org.springframework.data.domain.Pageable pageable);

}
