package com.example.YashToolBackEnd.enquiry.repository;

import com.example.YashToolBackEnd.enquiry.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
}
