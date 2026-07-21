package com.kalibyte.YashTools.production.logistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryChallanItemRequest {
    @NotNull(message = "Work Order Item ID is required")
    private UUID workOrderItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;
}
