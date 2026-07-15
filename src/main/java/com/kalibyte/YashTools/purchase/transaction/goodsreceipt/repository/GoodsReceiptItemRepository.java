package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.repository;

import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, UUID> {
    List<GoodsReceiptItem> findByPoItemReferenceId(UUID poItemReferenceId);

    @Query("SELECT COALESCE(SUM(gri.currentReceivedQuantity), 0) FROM GoodsReceiptItem gri WHERE gri.poItemReference.id = :poItemId")
    BigDecimal sumReceivedQuantityByPoItemId(@Param("poItemId") UUID poItemId);
}
