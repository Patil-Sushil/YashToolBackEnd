package com.kalibyte.YashTools.workorder.repository;

import com.kalibyte.YashTools.workorder.entity.WorkOrder;
import com.kalibyte.YashTools.workorder.entity.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkOrderRepository 
        extends JpaRepository<WorkOrder, UUID>, JpaSpecificationExecutor<WorkOrder> {
        
    Optional<WorkOrder> findByWorkOrderNo(String workOrderNo);
    Optional<WorkOrder> findByIdAndCompanyId(UUID id, UUID companyId);
    boolean existsByQuotationId(UUID quotationId);
    long countByStatus(WorkOrderStatus status);


    @org.springframework.data.jpa.repository.Query("SELECT w FROM WorkOrder w WHERE w.company.id = :companyId AND (" +
            "LOWER(w.workOrderNo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(w.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(w.remarks) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(w.customerCompanyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<WorkOrder> searchWorkOrders(
            @org.springframework.data.repository.query.Param("companyId") UUID companyId,
            @org.springframework.data.repository.query.Param("search") String search,
            org.springframework.data.domain.Pageable pageable);

}
