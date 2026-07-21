package com.kalibyte.YashTools.production.packing.repository;

import com.kalibyte.YashTools.production.packing.entity.PackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackingLogRepository extends JpaRepository<PackingLog, UUID> {
    Optional<PackingLog> findByIdAndCompanyId(UUID id, UUID companyId);
    List<PackingLog> findByWorkOrderItemIdAndCompanyId(UUID workOrderItemId, UUID companyId);
}
