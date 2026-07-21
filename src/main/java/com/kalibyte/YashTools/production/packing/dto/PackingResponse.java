package com.kalibyte.YashTools.production.packing.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PackingResponse {
    private UUID id;
    private UUID workOrderItemId;
    private String workOrderNo;
    private String toolName;
    private String itemName;
    private String packingNo;
    private String batchNo;
    private String packageSize;
    private Integer quantity;
    private String containerStatus;
    private String remarks;
    private LocalDateTime createdAt;
    private String createdBy;
}
