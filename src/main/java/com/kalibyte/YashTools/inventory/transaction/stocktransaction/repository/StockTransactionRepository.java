package com.kalibyte.YashTools.inventory.transaction.stocktransaction.repository;

import com.kalibyte.YashTools.inventory.transaction.stocktransaction.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID>, JpaSpecificationExecutor<StockTransaction> {
}
