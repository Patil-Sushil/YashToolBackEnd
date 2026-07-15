package com.kalibyte.YashTools.workorder.repository;

import com.kalibyte.YashTools.workorder.entity.WorkOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkOrderItemRepository extends JpaRepository<WorkOrderItem, UUID> {
}
