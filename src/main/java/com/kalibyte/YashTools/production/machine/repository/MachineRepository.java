package com.kalibyte.YashTools.production.machine.repository;

import com.kalibyte.YashTools.production.machine.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineRepository 
        extends JpaRepository<Machine, UUID>, JpaSpecificationExecutor<Machine> {
    Optional<Machine> findByIdAndCompanyId(UUID id, UUID companyId);
    Optional<Machine> findByCodeAndCompanyId(String code, UUID companyId);
    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}
