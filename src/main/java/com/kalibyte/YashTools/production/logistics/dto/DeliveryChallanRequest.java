package com.kalibyte.YashTools.production.logistics.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryChallanRequest {
    @NotNull(message = "Work Order ID is required")
    private UUID workOrderId;

    private String vehicleNo;
    private String driverName;
    private String driverContact;
    private String remarks;

    @NotEmpty(message = "Items to deliver are required")
    private List<DeliveryChallanItemRequest> items;
}
