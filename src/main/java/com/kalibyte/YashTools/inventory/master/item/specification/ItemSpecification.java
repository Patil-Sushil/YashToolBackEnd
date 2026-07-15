package com.kalibyte.YashTools.inventory.master.item.specification;

import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ItemSpecification {

    public static Specification<Item> hasCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Item> hasMaterialGrade(UUID materialGradeId) {
        return (root, query, cb) -> materialGradeId == null ? null : cb.equal(root.get("materialGrade").get("id"), materialGradeId);
    }

    public static Specification<Item> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Item> search(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return null;
            }
            String pattern = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("sku")), pattern)
            );
        };
    }
}
