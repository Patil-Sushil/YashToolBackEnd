package com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response;

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
public class PurchaseReturnItemResponse {
    private UUID id;
    private UUID itemId;
    private String itemName;
    private String itemSku;
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal rate;
    private String remarks;
}
