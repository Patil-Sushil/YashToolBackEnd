package com.kalibyte.YashTools.quotation.repository;

import com.kalibyte.YashTools.quotation.entity.QuotationApproval;
import com.kalibyte.YashTools.quotation.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationApprovalRepository extends JpaRepository<QuotationApproval, UUID> {

    Optional<QuotationApproval> findTopByQuotationIdOrderByRequestedAtDesc(UUID quotationId);

    List<QuotationApproval> findByApprovalStatus(ApprovalStatus status);
}