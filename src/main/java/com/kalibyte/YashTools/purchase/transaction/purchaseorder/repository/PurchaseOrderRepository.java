package com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository;

import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID>, JpaSpecificationExecutor<PurchaseOrder> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    @Query("SELECT po FROM PurchaseOrder po ORDER BY po.createdAt DESC LIMIT 1")
    Optional<PurchaseOrder> findTopByOrderByCreatedAtDesc();


    @org.springframework.data.jpa.repository.Query("SELECT p FROM PurchaseOrder p WHERE " +
            "LOWER(p.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.remarks) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.vendor.vendorName) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<PurchaseOrder> searchPurchaseOrders(
            @org.springframework.data.repository.query.Param("search") String search);

}
