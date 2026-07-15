package com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response;

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
public class PurchaseOrderItemResponse {
    private UUID id;
    private UUID itemId;
    private String itemName;
    private String itemSku;
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    private BigDecimal orderedQuantity;
    private String unit;
    private BigDecimal rate;
    private BigDecimal gstPercentage;
    private BigDecimal discount;
    private BigDecimal lineTotal;
}
