package com.kalibyte.YashTools.email.repository;

import com.kalibyte.YashTools.email.entity.EmailLog;
import com.kalibyte.YashTools.email.entity.enums.EmailStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EmailLogSpecification {
    private EmailLogSpecification() {}

    public static Specification<EmailLog> withEntity(String type, UUID id) {
        return (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (type != null) p.add(cb.equal(root.get("entityType"), type));
            if (id != null) p.add(cb.equal(root.get("entityId"), id));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    public static Specification<EmailLog> withStatus(EmailStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("mailStatus"), status);
    }
}