package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response;

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
public class PurchaseInvoiceItemResponse {
    private UUID id;
    private UUID itemId;
    private String itemName;
    private String itemSku;
    private UUID materialGradeId;
    private String materialGradeName;
    private String materialGradeCode;
    private BigDecimal invoiceQuantity;
    private BigDecimal invoiceRate;
    private BigDecimal gst;
    private BigDecimal taxableAmount;
    private BigDecimal totalAmount;
}
