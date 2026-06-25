package com.kalibyte.YashTools.master.rawmaterial.repository;

import com.kalibyte.YashTools.master.rawmaterial.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, UUID> {

    List<RawMaterial> findByActiveTrue();

    Optional<RawMaterial> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}