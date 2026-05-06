package com.kalibyte.YashTools.master.coating.repository;

import com.kalibyte.YashTools.master.coating.entity.Coating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoatingRepository extends JpaRepository<Coating, Long> {
    List<Coating> findByActiveTrue();
}
