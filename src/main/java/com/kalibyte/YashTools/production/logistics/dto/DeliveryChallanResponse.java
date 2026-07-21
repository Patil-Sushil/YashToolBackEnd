package com.kalibyte.YashTools.production.logistics.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryChallanResponse {
    private UUID id;
    private String challanNo;
    private UUID workOrderId;
    private String workOrderNo;
    private String customerName;
    private String vehicleNo;
    private String driverName;
    private String driverContact;
    private String status;
    private LocalDate deliveryDate;
    private String deliveryReceiptBy;
    private LocalDateTime deliveryReceiptAt;
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
    }
}
