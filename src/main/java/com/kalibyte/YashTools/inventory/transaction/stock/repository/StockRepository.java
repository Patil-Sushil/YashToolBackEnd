package com.kalibyte.YashTools.inventory.transaction.stock.repository;

import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.transaction.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    @Query("SELECT s FROM Stock s WHERE s.item = :item AND " +
           "((:materialGrade IS NULL AND s.materialGrade IS NULL) OR s.materialGrade = :materialGrade)")
    Optional<Stock> findStock(@Param("item") Item item, 
                              @Param("materialGrade") MaterialGrade materialGrade);
}
