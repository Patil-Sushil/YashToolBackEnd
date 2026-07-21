package com.kalibyte.YashTools.production.packing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PackingRequest {
    @NotNull(message = "Work Order Item ID is required")
    private UUID workOrderItemId;

    @NotBlank(message = "Batch No is required")
    private String batchNo;

    @NotBlank(message = "Package size is required")
    private String packageSize;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Container status is required")
    private String containerStatus;

    private String remarks;
}
