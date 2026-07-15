package com.kalibyte.YashTools.inventory.transaction.stocktake.repository;

import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockTakeRepository extends JpaRepository<StockTake, UUID> {
    Optional<StockTake> findByStockTakeNumber(String number);
}
