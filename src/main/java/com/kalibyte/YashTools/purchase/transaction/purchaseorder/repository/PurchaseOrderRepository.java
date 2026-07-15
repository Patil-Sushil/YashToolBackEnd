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
}
