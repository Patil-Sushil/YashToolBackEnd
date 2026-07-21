package com.kalibyte.YashTools.audit.repository;


import com.kalibyte.YashTools.audit.dto.response.UserActivityProjection;
import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends
        JpaRepository<AuditLog, UUID>,
        JpaSpecificationExecutor<AuditLog> {

    // ============================================================
    // Derived queries
    // ============================================================

    Page<AuditLog> findByUserIdOrderByTimestampDesc(
            UUID userId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, UUID entityId);

    List<AuditLog> findByTimestampAfterOrderByTimestampDesc(
            LocalDateTime since);

    void deleteByTimestampBefore(LocalDateTime cutoffDate);

    // ============================================================
    // Statistics
    // ============================================================

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = :status")
    Long countByStatus(@Param("status") AuditStatus status);

    @Query("""
            SELECT COUNT(DISTINCT a.userId)
            FROM AuditLog a
            WHERE a.userId IS NOT NULL
            """)
    Long countUniqueUsers();

    @Query("""
            SELECT a.action, COUNT(a)
            FROM AuditLog a
            GROUP BY a.action
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> countGroupedByAction();

    @Query("""
            SELECT a.entityType, COUNT(a)
            FROM AuditLog a
            WHERE a.entityType IS NOT NULL
            GROUP BY a.entityType
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> countGroupedByEntityType();

    @Query("""
            SELECT a.username, COUNT(a)
            FROM AuditLog a
            WHERE a.username IS NOT NULL
            GROUP BY a.username
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> getTopUsersByActivity();

    @Query("""
            SELECT
                al.userId   as userId,
                al.username as username,
                COUNT(al)   as totalActions,
                MAX(al.timestamp) as lastActivity,
                MIN(al.timestamp) as firstActivity
            FROM AuditLog al
            WHERE al.username IS NOT NULL
            GROUP BY al.userId, al.username
            ORDER BY COUNT(al) DESC
            """)
    List<UserActivityProjection> getTopUsersByActivityProjection();

    @Query("""
            SELECT al.action, COUNT(al) as count
            FROM AuditLog al
            WHERE al.username = :username
            GROUP BY al.action
            ORDER BY COUNT(al) DESC
            """)
    List<Object[]> getTopActionsByUser(@Param("username") String username);

    @Query("""
            SELECT FUNCTION('date', a.timestamp), COUNT(a)
            FROM AuditLog a
            WHERE a.timestamp >= :since
            GROUP BY FUNCTION('date', a.timestamp)
            ORDER BY FUNCTION('date', a.timestamp) DESC
            """)
    List<Object[]> getDailyActivity(@Param("since") LocalDateTime since);
}
