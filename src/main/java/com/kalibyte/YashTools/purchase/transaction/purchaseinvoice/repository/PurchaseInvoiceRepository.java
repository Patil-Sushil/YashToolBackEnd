package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.repository;

import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, UUID> {
    Optional<PurchaseInvoice> findByInvoiceNumber(String invoiceNumber);
    Optional<PurchaseInvoice> findBySupplierInvoiceNumber(String supplierInvoiceNumber);

    List<PurchaseInvoice> findByStatusAndOutstandingAmountGreaterThan(
            com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus status, 
            java.math.BigDecimal amount);

    List<PurchaseInvoice> findByVendorIdAndStatusAndOutstandingAmountGreaterThan(
            UUID vendorId, 
            com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus status, 
            java.math.BigDecimal amount);

    List<PurchaseInvoice> findByStatusAndOutstandingAmountGreaterThanAndDueDateLessThan(
            com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus status, 
            java.math.BigDecimal amount, 
            java.time.LocalDate date);

    @Query("SELECT pi FROM PurchaseInvoice pi ORDER BY pi.createdAt DESC LIMIT 1")
    Optional<PurchaseInvoice> findTopByOrderByCreatedAtDesc();
}
