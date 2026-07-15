package com.kalibyte.YashTools.inventory.master.materialgrade.repository;

import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialGradeRepository extends JpaRepository<MaterialGrade, UUID> {
    Optional<MaterialGrade> findByCode(String code);
    Optional<MaterialGrade> findByName(String name);
}
