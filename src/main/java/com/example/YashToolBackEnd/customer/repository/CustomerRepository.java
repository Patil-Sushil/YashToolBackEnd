package com.example.YashToolBackEnd.customer.repository;

import com.example.YashToolBackEnd.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer,Long> {

    Optional<Customer> findByMobileNumberAndIsDeletedFalse(String mobileNumber);

    Optional<Customer> findByEmailAndIsDeletedFalse(String email);

    List<Customer> findAllByIsDeletedFalse();
}
