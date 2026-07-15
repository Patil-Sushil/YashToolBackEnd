package com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request;

import com.kalibyte.YashTools.purchase.shared.enums.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePurchaseOrderStatusRequest {

    @NotNull(message = "Status is required")
    private PurchaseOrderStatus status;

    private String remarks;
}
