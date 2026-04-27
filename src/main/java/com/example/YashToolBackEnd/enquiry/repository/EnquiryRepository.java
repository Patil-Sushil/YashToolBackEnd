package com.example.YashToolBackEnd.enquiry.repository;

import com.example.YashToolBackEnd.enquiry.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
}
