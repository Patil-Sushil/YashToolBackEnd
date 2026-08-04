package com.kalibyte.YashTools.production.logistics.damage.dto;

import com.kalibyte.YashTools.production.logistics.damage.entity.enums.TransitDamageAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransitDamageRequest {

    @NotNull(message = "Delivery Challan ID is required")
    private UUID deliveryChallanId;

    @NotNull(message = "Work Order Item ID is required")
    private UUID workOrderItemId;

    @NotNull(message = "Damaged quantity is required")
    @Min(value = 1, message = "Damaged quantity must be at least 1")
    private Integer damagedQuantity;

    @NotNull(message = "Action type is required (REWORK, REPLACE, CREDIT_ONLY)")
    private TransitDamageAction action;

    private String remarks;
}
