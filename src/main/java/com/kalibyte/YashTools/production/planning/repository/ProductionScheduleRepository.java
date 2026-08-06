package com.kalibyte.YashTools.production.planning.repository;

import com.kalibyte.YashTools.production.planning.entity.ProductionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductionScheduleRepository 
        extends JpaRepository<ProductionSchedule, UUID>, JpaSpecificationExecutor<ProductionSchedule> {
    Optional<ProductionSchedule> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<ProductionSchedule> findByJobCardId(UUID jobCardId);
    Optional<ProductionSchedule> findByJobCardIdAndCompanyId(UUID jobCardId, UUID companyId);
    boolean existsByJobCardId(UUID jobCardId);
    java.util.List<ProductionSchedule> findByMachineIdAndCompanyId(UUID machineId, UUID companyId);
}
