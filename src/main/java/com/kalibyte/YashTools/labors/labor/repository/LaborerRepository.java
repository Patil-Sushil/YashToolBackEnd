package com.kalibyte.YashTools.labors.labor.repository;

import com.kalibyte.YashTools.labors.labor.entity.Enum.LaborRole;
import com.kalibyte.YashTools.labors.labor.entity.Laborer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborerRepository extends JpaRepository<Laborer, Long> {
    List<Laborer> findByIsActiveTrue();
    List<Laborer> findByRoleAndIsActiveTrue(LaborRole role);
}
