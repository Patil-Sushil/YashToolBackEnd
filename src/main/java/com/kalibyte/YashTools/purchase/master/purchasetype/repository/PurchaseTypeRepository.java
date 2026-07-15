package com.kalibyte.YashTools.purchase.master.purchasetype.repository;

import com.kalibyte.YashTools.purchase.master.purchasetype.entity.PurchaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseTypeRepository extends JpaRepository<PurchaseType, UUID> {
    Optional<PurchaseType> findByCode(String code);
}
