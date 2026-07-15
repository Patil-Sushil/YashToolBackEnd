package com.kalibyte.YashTools.inventory.master.item.repository;

import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {
    Optional<Item> findBySku(String sku);
}
