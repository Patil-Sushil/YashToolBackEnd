package com.kalibyte.YashTools.enquiry.repository;

import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
}
