package com.kalibyte.YashTools.enquiry.repository;

import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Enquiry entity
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    /**
     * Find most recent enquiry for sequence generation
     *
     * @return latest enquiry if exists
     */
    Optional<Enquiry> findTopByOrderByCreatedAtDesc();

    /**
     * Find all enquiries for a specific customer
     *
     * @param customerId customer identifier
     * @return list of customer enquiries
     */
    @Query("SELECT e FROM Enquiry e WHERE e.customer.id = :customerId ORDER BY e.createdAt DESC")
    List<Enquiry> findByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Find enquiries by status
     *
     * @param status enquiry status
     * @return list of enquiries with given status
     */
    List<Enquiry> findByStatus(EnquiryStatus status);

    /**
     * Find urgent enquiries
     *
     * @return list of urgent enquiries
     */
    List<Enquiry> findByIsUrgentTrue();

    /**
     * Find enquiries created between dates
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of enquiries in date range
     */
    @Query("SELECT e FROM Enquiry e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    List<Enquiry> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Check if enquiry exists by number
     *
     * @param enquiryNo enquiry number
     * @return true if exists
     */
    boolean existsByEnquiryNo(String enquiryNo);
}