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
}
