package com.kalibyte.YashTools.purchase.transaction.purchasereturn.repository;

import com.kalibyte.YashTools.purchase.transaction.purchasereturn.entity.PurchaseReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, UUID> {
    Optional<PurchaseReturn> findByReturnNumber(String returnNumber);

    @Query("SELECT pr FROM PurchaseReturn pr ORDER BY pr.createdAt DESC LIMIT 1")
    Optional<PurchaseReturn> findTopByOrderByCreatedAtDesc();
}
