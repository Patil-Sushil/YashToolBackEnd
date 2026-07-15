package com.kalibyte.YashTools.purchase.transaction.vendorpayment.repository;

import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VendorPaymentItemRepository extends JpaRepository<VendorPaymentItem, UUID> {
}
