package com.kalibyte.YashTools.sales.invoice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesInvoiceResponse {
    private UUID id;
    private String invoiceNo;
    private UUID workOrderId;
    private String workOrderNo;
    private String customerName;
    private LocalDate invoiceDate;
    private BigDecimal subTotal;
    private BigDecimal cgstRate;
    private BigDecimal cgstAmount;
    private BigDecimal sgstRate;
    private BigDecimal sgstAmount;
    private BigDecimal igstRate;
    private BigDecimal igstAmount;
    private BigDecimal totalAmount;
    private String status;
    private String remarks;
    private UUID companyId;
    private String companyCode;
    private String customerEmail;
    private List<ItemResponse> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private UUID id;
        private UUID workOrderItemId;
        private String toolName;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
