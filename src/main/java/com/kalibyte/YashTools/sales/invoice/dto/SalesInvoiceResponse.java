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
    private UUID quotationId;
    private String quotationNo;
    private String poNumber;
    private String shippingAddress;
    private String customerContactPerson;
    private String customerEmail;
    private String customerMobile;
    private List<ItemResponse> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private UUID id;
        private UUID workOrderItemId;
        private String toolName;
        private String itemName;
        private Integer quantity;
        private Integer orderedQuantity;
        private Integer alreadyInvoicedQuantity;
        private Integer remainingUninvoicedQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Double diameter;
        private Double shankDiameter;
        private Double overallLength;
        private Double fluteLength;
        private String drawingReference;
        private String materialGrade;
        private String materialType;
        private String coatingType;
    }
}
