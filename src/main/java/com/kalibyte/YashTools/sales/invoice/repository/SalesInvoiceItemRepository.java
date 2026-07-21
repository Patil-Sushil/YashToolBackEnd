package com.kalibyte.YashTools.sales.invoice.repository;

import com.kalibyte.YashTools.sales.invoice.entity.SalesInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SalesInvoiceItemRepository extends JpaRepository<SalesInvoiceItem, UUID> {

    @Query("SELECT COALESCE(SUM(sii.quantity), 0) FROM SalesInvoiceItem sii " +
           "WHERE sii.workOrderItem.id = :workOrderItemId " +
           "AND sii.salesInvoice.status != 'CANCELLED'")
    int getSumQuantityByWorkOrderItemId(@Param("workOrderItemId") UUID workOrderItemId);
}
