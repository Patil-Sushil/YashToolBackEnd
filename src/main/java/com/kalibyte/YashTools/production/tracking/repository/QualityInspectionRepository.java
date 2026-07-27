package com.kalibyte.YashTools.production.tracking.repository;

import com.kalibyte.YashTools.production.tracking.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, UUID>, JpaSpecificationExecutor<QualityInspection> {
    Optional<QualityInspection> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<QualityInspection> findByJobCardIdAndCompanyId(UUID jobCardId, UUID companyId);
    boolean existsByJobCardId(UUID jobCardId);
}
