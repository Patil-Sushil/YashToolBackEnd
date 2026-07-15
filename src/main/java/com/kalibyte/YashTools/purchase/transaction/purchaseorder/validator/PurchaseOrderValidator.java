package com.kalibyte.YashTools.purchase.transaction.purchaseorder.validator;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderValidator {

    public void validateForSave(PurchaseOrder po) {
        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new BusinessException("Purchase order must contain at least one item.");
        }
        
        po.getItems().forEach(item -> {
            if (item.getOrderedQuantity() == null || item.getOrderedQuantity().doubleValue() <= 0) {
                throw new BusinessException("Ordered quantity must be greater than zero.");
            }
            if (item.getRate() == null || item.getRate().doubleValue() < 0) {
                throw new BusinessException("Rate must be non-negative.");
            }
        });
    }

    public void validateStatusTransition(PurchaseOrderStatus current, PurchaseOrderStatus target) {
        if (current == target) {
            return;
        }

        if (current == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("Cancelled Purchase Orders cannot be edited or transitioned.");
        }

        if (current == PurchaseOrderStatus.CLOSED) {
            throw new BusinessException("Closed Purchase Orders cannot be transitioned.");
        }

        boolean allowed = switch (current) {
            case DRAFT -> target == PurchaseOrderStatus.APPROVED || target == PurchaseOrderStatus.CANCELLED;
            case APPROVED -> target == PurchaseOrderStatus.PARTIALLY_RECEIVED 
                    || target == PurchaseOrderStatus.FULLY_RECEIVED 
                    || target == PurchaseOrderStatus.CLOSED 
                    || target == PurchaseOrderStatus.CANCELLED;
            case PARTIALLY_RECEIVED -> target == PurchaseOrderStatus.FULLY_RECEIVED 
                    || target == PurchaseOrderStatus.CLOSED 
                    || target == PurchaseOrderStatus.CANCELLED;
            case FULLY_RECEIVED -> target == PurchaseOrderStatus.CLOSED;
            default -> false;
        };

        if (!allowed) {
            throw new BusinessException(String.format("Invalid status transition from %s to %s.", current, target));
        }
    }
}
