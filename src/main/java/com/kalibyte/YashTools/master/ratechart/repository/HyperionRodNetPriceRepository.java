package com.kalibyte.YashTools.master.ratechart.repository;

import com.kalibyte.YashTools.master.ratechart.entity.HyperionRodNetPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HyperionRodNetPriceRepository extends JpaRepository<HyperionRodNetPrice, UUID> {

    List<HyperionRodNetPrice> findByActiveTrue();

    Page<HyperionRodNetPrice> findByActiveTrue(Pageable pageable);

    @Query("SELECT h FROM HyperionRodNetPrice h WHERE LOWER(h.item) = LOWER(:item)")
    Optional<HyperionRodNetPrice> findByItemIgnoreCase(String item);

    @Query("SELECT h FROM HyperionRodNetPrice h WHERE LOWER(h.item) = LOWER(:item) AND h.active = true")
    Optional<HyperionRodNetPrice> findByItemIgnoreCaseAndActiveTrue(String item);

    boolean existsByItemIgnoreCase(String item);
}