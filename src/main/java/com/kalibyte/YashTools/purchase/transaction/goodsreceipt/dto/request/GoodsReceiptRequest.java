package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request;

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
public class GoodsReceiptRequest {

    @NotNull(message = "Purchase Order ID is required")
    private UUID purchaseOrderId;

    private LocalDate grnDate;

    private String warehouse;

    private String receivedBy;

    private String remarks;

    @NotEmpty(message = "Goods Receipt must contain at least one item")
    @Valid
    private List<GoodsReceiptItemRequest> items;
}
