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
}
