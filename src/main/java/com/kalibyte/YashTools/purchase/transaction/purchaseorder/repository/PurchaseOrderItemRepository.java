package com.kalibyte.YashTools.purchase.transaction.purchaseorder.repository;

import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
}
