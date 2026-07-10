package com.kalibyte.YashTools.email.repository;

import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID>, JpaSpecificationExecutor<EmailLog> {

    List<EmailLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
    Optional<EmailLog> findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    @Query("SELECT e FROM EmailLog e WHERE e.mailStatus = :status AND e.retryCount < e.maxRetries ORDER BY e.priority DESC, e.queuedAt ASC")
    List<EmailLog> findRetryable(@Param("status") EmailStatus status, Pageable pageable);

    @Query("SELECT e FROM EmailLog e WHERE e.mailStatus = :status AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)")
    List<EmailLog> findPendingRetries(@Param("status") EmailStatus status, @Param("now") LocalDateTime now);

    long countByEntityTypeAndEntityIdAndMailStatus(String entityType, UUID entityId, EmailStatus status);
}