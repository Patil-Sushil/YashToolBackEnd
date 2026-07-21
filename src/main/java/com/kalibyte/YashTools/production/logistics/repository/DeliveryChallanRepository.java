package com.kalibyte.YashTools.production.logistics.repository;

import com.kalibyte.YashTools.production.logistics.entity.DeliveryChallan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, UUID> {
    Optional<DeliveryChallan> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<DeliveryChallan> findByChallanNoAndCompanyId(String challanNo, UUID companyId);
}
