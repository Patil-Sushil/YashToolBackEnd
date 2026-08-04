package com.kalibyte.YashTools.production.logistics.damage.repository;

import com.kalibyte.YashTools.production.logistics.damage.entity.TransitDamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransitDamageReportRepository extends 
        JpaRepository<TransitDamageReport, UUID>, JpaSpecificationExecutor<TransitDamageReport> {
    
    Optional<TransitDamageReport> findByIdAndCompanyId(UUID id, UUID companyId);
    List<TransitDamageReport> findByDeliveryChallanIdAndCompanyId(UUID challanId, UUID companyId);
}
