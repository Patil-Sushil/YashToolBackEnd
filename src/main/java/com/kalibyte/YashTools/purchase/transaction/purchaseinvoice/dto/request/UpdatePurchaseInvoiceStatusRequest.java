package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request;

import com.kalibyte.YashTools.purchase.shared.enums.PurchaseInvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePurchaseInvoiceStatusRequest {

    @NotNull(message = "Status is required")
    private PurchaseInvoiceStatus status;

    private String remarks;
}
