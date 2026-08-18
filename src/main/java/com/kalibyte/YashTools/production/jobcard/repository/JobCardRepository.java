package com.kalibyte.YashTools.production.jobcard.repository;

import com.kalibyte.YashTools.production.jobcard.entity.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, UUID>, JpaSpecificationExecutor<JobCard> {
    Optional<JobCard> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<JobCard> findByJobCardNoAndCompanyId(String jobCardNo, UUID companyId);
    List<JobCard> findByWorkOrderItemIdAndCompanyId(UUID workOrderItemId, UUID companyId);
    List<JobCard> findByWorkOrderIdAndCompanyId(UUID workOrderId, UUID companyId);
    
    @Query("SELECT COALESCE(SUM(j.totalQuantity), 0) FROM JobCard j WHERE j.workOrderItem.id = :itemId AND j.company.id = :companyId AND j.status <> 'CANCELLED'")
    int getSumQuantityByWorkOrderItemId(@Param("itemId") UUID itemId, @Param("companyId") UUID companyId);
}
