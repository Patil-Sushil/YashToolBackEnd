package com.kalibyte.YashTools.inventory.transaction.stocktake.repository;

import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTakeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockTakeLineRepository extends JpaRepository<StockTakeLine, UUID> {
}
