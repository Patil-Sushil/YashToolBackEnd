package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository;

import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
    Optional<GoodsReceipt> findByGrnNumber(String grnNumber);

    List<GoodsReceipt> findByPurchaseOrderId(UUID purchaseOrderId);

    @Query("SELECT gr FROM GoodsReceipt gr ORDER BY gr.createdAt DESC LIMIT 1")
    Optional<GoodsReceipt> findTopByOrderByCreatedAtDesc();
}
