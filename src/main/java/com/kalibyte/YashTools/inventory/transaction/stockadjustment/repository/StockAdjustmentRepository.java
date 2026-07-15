package com.kalibyte.YashTools.inventory.transaction.stockadjustment.repository;

import com.kalibyte.YashTools.inventory.transaction.stockadjustment.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, UUID> {
    Optional<StockAdjustment> findByAdjustmentNumber(String adjustmentNumber);
}
