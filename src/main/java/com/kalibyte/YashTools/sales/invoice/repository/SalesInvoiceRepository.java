package com.kalibyte.YashTools.sales.invoice.repository;

import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID>, JpaSpecificationExecutor<SalesInvoice> {
    Optional<SalesInvoice> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<SalesInvoice> findByInvoiceNoAndCompanyId(String invoiceNo, UUID companyId);
}
