package com.kalibyte.YashTools.production.logistics.repository;

import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryChallanItemRepository extends JpaRepository<DeliveryChallanItem, UUID> {

    @Query("SELECT COALESCE(SUM(dci.quantity), 0) FROM DeliveryChallanItem dci " +
           "WHERE dci.workOrderItem.id = :workOrderItemId " +
           "AND dci.deliveryChallan.status != 'CANCELLED'")
    int getSumQuantityByWorkOrderItemId(@Param("workOrderItemId") UUID workOrderItemId);
}
