package com.kalibyte.YashTools.sales.invoice.repository;

import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID>, JpaSpecificationExecutor<SalesInvoice> {
    Optional<SalesInvoice> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<SalesInvoice> findByInvoiceNoAndCompanyId(String invoiceNo, UUID companyId);
    List<SalesInvoice> findByCompanyIdAndInvoiceDateBetween(UUID companyId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(si.totalAmount), 0) FROM SalesInvoice si")
    java.math.BigDecimal getTotalRevenue();
}
