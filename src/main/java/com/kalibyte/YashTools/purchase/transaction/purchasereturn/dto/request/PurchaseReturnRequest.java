package com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnRequest {

    @NotNull(message = "Goods Receipt ID is required")
    private UUID goodsReceiptId;

    private UUID purchaseInvoiceId;

    private LocalDate returnDate;

    private String remarks;

    @NotEmpty(message = "Purchase Return must contain at least one item")
    @Valid
    private List<PurchaseReturnItemRequest> items;
}
