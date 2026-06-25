package com.kalibyte.YashTools.master.ratechart.repository;

import com.kalibyte.YashTools.master.ratechart.entity.HyperionCoolantHoleRodPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HyperionCoolantHoleRodPriceRepository extends JpaRepository<HyperionCoolantHoleRodPrice, UUID> {

    List<HyperionCoolantHoleRodPrice> findByActiveTrue();

    Page<HyperionCoolantHoleRodPrice> findByActiveTrue(Pageable pageable);

    @Query("SELECT h FROM HyperionCoolantHoleRodPrice h WHERE " +
            "LOWER(h.category) = LOWER(:category) AND LOWER(h.item) = LOWER(:item)")
    Optional<HyperionCoolantHoleRodPrice> findByCategoryIgnoreCaseAndItemIgnoreCase(String category, String item);

    boolean existsByCategoryIgnoreCaseAndItemIgnoreCase(String category, String item);

    List<HyperionCoolantHoleRodPrice> findByCategoryIgnoreCase(String category);
}