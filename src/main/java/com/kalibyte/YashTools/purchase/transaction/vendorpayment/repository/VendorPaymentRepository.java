package com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository;

import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorPaymentRepository extends JpaRepository<VendorPayment, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<VendorPayment> {
    Optional<VendorPayment> findByPaymentNumber(String paymentNumber);

    boolean existsByCompanyIdAndPaymentReferenceNumber(UUID companyId, String paymentReferenceNumber);

    boolean existsByCompanyIdAndPaymentReferenceNumberAndIdNot(UUID companyId, String paymentReferenceNumber, UUID id);

    @Query("SELECT vp FROM VendorPayment vp ORDER BY vp.createdAt DESC LIMIT 1")
    Optional<VendorPayment> findTopByOrderByCreatedAtDesc();

    java.util.List<VendorPayment> findByVendorId(UUID vendorId);
}
