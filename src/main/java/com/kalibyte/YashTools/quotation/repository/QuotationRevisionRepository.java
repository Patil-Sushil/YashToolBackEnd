package com.kalibyte.YashTools.quotation.repository;

import com.kalibyte.YashTools.quotation.entity.QuotationRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRevisionRepository extends JpaRepository<QuotationRevision, UUID> {

    List<QuotationRevision> findByQuotationIdOrderByVersionNumberDesc(UUID quotationId);

    Optional<QuotationRevision> findTopByQuotationIdOrderByVersionNumberDesc(UUID quotationId);
}