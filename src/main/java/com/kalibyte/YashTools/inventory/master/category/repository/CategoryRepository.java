package com.kalibyte.YashTools.inventory.master.category.repository;

import com.kalibyte.YashTools.inventory.master.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByCode(String code);
    Optional<Category> findByName(String name);
}
