package com.kalibyte.YashTools.inventory.transaction.stocktransaction.specification;

import com.kalibyte.YashTools.inventory.shared.enums.StockTransactionType;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.entity.StockTransaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class StockTransactionSpecification {

    public static Specification<StockTransaction> hasTransactionType(StockTransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("transactionType"), type);
    }

    public static Specification<StockTransaction> hasItem(UUID itemId) {
        return (root, query, cb) -> itemId == null ? null : cb.equal(root.get("item").get("id"), itemId);
    }

    public static Specification<StockTransaction> hasReferenceNumber(String ref) {
        return (root, query, cb) -> (ref == null || ref.trim().isEmpty()) ? null : cb.equal(root.get("referenceNumber"), ref.trim());
    }
}
