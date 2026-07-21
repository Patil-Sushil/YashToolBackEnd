package com.kalibyte.YashTools.production.execution.repository;

import com.kalibyte.YashTools.production.execution.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExecutionLogRepository 
        extends JpaRepository<ExecutionLog, UUID>, JpaSpecificationExecutor<ExecutionLog> {
    Optional<ExecutionLog> findByJobCardIdAndEndTimeIsNull(UUID jobCardId);
    Optional<ExecutionLog> findByOperatorIdAndEndTimeIsNull(Long operatorId);
    List<ExecutionLog> findByJobCardId(UUID jobCardId);
}
