package com.kalibyte.YashTools.audit.repository;


import com.kalibyte.YashTools.audit.entity.AuditLog;
import com.kalibyte.YashTools.audit.entity.enums.AuditAction;
import com.kalibyte.YashTools.audit.entity.enums.AuditStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogSpecification {

    private AuditLogSpecification() {}

    public static Specification<AuditLog> withFilters(
            UUID userId,
            String username,
            AuditAction action,
            AuditStatus status,
            String entityType,
            UUID entityId,
            String ipAddress,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String search) {

        return (Root<AuditLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ── userId filter ─────────────────────────────────────────────
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            // ── username filter (case-insensitive partial match) ──────────
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%"
                ));
            }

            // ── action filter (exact enum match) ─────────────────────────
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            // ── status filter (exact enum match) ─────────────────────────
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // ── entityType filter (case-insensitive exact match) ──────────
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("entityType")),
                        entityType.toLowerCase()
                ));
            }

            // ── entityId filter ───────────────────────────────────────────
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }

            // ── ipAddress filter (exact match) ────────────────────────────
            if (ipAddress != null && !ipAddress.isBlank()) {
                predicates.add(cb.equal(root.get("ipAddress"), ipAddress));
            }

            // ── Date range: startDate ─────────────────────────────────────
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("timestamp"), startDate));
            }

            // ── Date range: endDate ───────────────────────────────────────
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("timestamp"), endDate));
            }

            // ── Global search across multiple string fields ───────────────
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";

                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(
                                        cb.coalesce(root.get("actionDescription"), "")),
                                pattern),
                        cb.like(cb.lower(
                                        cb.coalesce(root.get("entityName"), "")),
                                pattern),
                        cb.like(cb.lower(
                                        cb.coalesce(root.get("username"), "")),
                                pattern),
                        cb.like(cb.lower(
                                        cb.coalesce(root.get("entityType"), "")),
                                pattern)
                );
                predicates.add(searchPredicate);
            }

            // ── Empty predicates = fetch ALL logs (no restriction) ────────
            if (predicates.isEmpty()) {
                return cb.conjunction(); // SQL: 1=1 → returns everything
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
