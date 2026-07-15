package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItemResponse {
    private UUID id;
    private UUID poItemReferenceId;
    private UUID itemId;
    private String itemName;
    private String itemSku;
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    private BigDecimal orderedQuantity;
    private BigDecimal previouslyReceivedQuantity;
    private BigDecimal currentReceivedQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private BigDecimal pendingQuantity;
}
