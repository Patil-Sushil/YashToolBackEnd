package com.kalibyte.YashTools.master.ratechart.repository;

import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToolServiceRateMasterRepository extends JpaRepository<ToolServiceRateMaster, UUID> {

    List<ToolServiceRateMaster> findByActiveTrue();

    Page<ToolServiceRateMaster> findByActiveTrue(Pageable pageable);

    Optional<ToolServiceRateMaster> findByServiceCodeIgnoreCase(String serviceCode);

    boolean existsByServiceCodeIgnoreCaseAndCompanyId(String serviceCode, UUID companyId);

    @Query("SELECT r FROM ToolServiceRateMaster r WHERE r.active = true " +
           "AND r.serviceType = :serviceType " +
           "AND (:toolType IS NULL OR LOWER(r.toolType) = LOWER(:toolType) OR LOWER(r.toolType) = 'any tool' OR LOWER(r.toolType) = 'all') " +
           "AND LOWER(r.toolMaterial) = LOWER(:toolMaterial) " +
           "AND :diameter BETWEEN r.diameterFrom AND r.diameterTo " +
           "ORDER BY CASE WHEN LOWER(r.toolType) = LOWER(:toolType) THEN 0 ELSE 1 END ASC")
    List<ToolServiceRateMaster> findMatchingRates(
            @Param("serviceType") ServiceType serviceType,
            @Param("toolType") String toolType,
            @Param("toolMaterial") String toolMaterial,
            @Param("diameter") Double diameter);
}
