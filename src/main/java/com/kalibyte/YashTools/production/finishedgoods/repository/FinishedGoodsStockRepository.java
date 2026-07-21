package com.kalibyte.YashTools.production.finishedgoods.repository;

import com.kalibyte.YashTools.production.finishedgoods.entity.FinishedGoodsStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinishedGoodsStockRepository extends JpaRepository<FinishedGoodsStock, UUID> {

    Optional<FinishedGoodsStock> findByWorkOrderItemIdAndCompanyId(UUID workOrderItemId, UUID companyId);

    @Query("SELECT f FROM FinishedGoodsStock f WHERE f.workOrderItem.id = :workOrderItemId AND f.company.id = :companyId")
    Optional<FinishedGoodsStock> findStock(@Param("workOrderItemId") UUID workOrderItemId, @Param("companyId") UUID companyId);
}
