package com.kalibyte.YashTools.quotation.repository;

import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository
        extends JpaRepository<Quotation, UUID>, JpaSpecificationExecutor<Quotation> {

    Optional<Quotation> findByQuotationNo(String quotationNo);

    Optional<Quotation> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsBySourceEnquiryIdAndStatusIn(
            UUID sourceEnquiryId, List<QuotationStatus> statuses);

    @Query("SELECT q FROM Quotation q WHERE q.company.id = :companyId AND q.parentQuotation IS NULL")
    Page<Quotation> findRootQuotations(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT q FROM Quotation q WHERE q.parentQuotation.id = :parentId ORDER BY q.version DESC")
    List<Quotation> findRevisionsByParent(@Param("parentId") UUID parentId);

    long countByCompanyIdAndStatus(UUID companyId, QuotationStatus status);
    long countByStatus(QuotationStatus status);
    long countByCompanyIdAndParentQuotationIsNull(UUID companyId);
    long countByParentQuotationIsNull();


    @Query("SELECT q FROM Quotation q WHERE q.company.id = :companyId AND (" +
            "LOWER(q.quotationNo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.remarks) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.customer.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quotation> searchQuotations(
            @Param("companyId") UUID companyId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT q FROM Quotation q WHERE q.company.id = :companyId " +
            "AND q.status = com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus.LOCKED " +
            "AND q.isLocked = true " +
            "AND q.customer.id = :customerId " +
            "AND q.id NOT IN (SELECT w.quotation.id FROM com.kalibyte.YashTools.workorder.entity.WorkOrder w) " +
            "ORDER BY q.lockedAt DESC")
    List<Quotation> findLockedQuotationsForCustomer(
            @Param("companyId") UUID companyId,
            @Param("customerId") UUID customerId);

    @Query("SELECT q FROM Quotation q WHERE q.company.id = :companyId " +
            "AND q.status = com.kalibyte.YashTools.quotation.entity.enums.QuotationStatus.LOCKED " +
            "AND q.isLocked = true " +
            "AND q.id NOT IN (SELECT w.quotation.id FROM com.kalibyte.YashTools.workorder.entity.WorkOrder w) " +
            "ORDER BY q.customer.companyName ASC, q.lockedAt DESC")
    List<Quotation> findAllLockedQuotationsAvailable(
            @Param("companyId") UUID companyId);

}