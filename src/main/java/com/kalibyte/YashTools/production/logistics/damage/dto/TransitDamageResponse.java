package com.kalibyte.YashTools.production.logistics.damage.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitDamageResponse {
    private UUID id;
    private UUID deliveryChallanId;
    private String deliveryChallanNo;
    private UUID workOrderItemId;
    private String toolName;
    private String itemName;
    private Integer damagedQuantity;
    private String action;
    private String status;
    private String reportedBy;
    private LocalDateTime reportedAt;
    private String remarks;
    private UUID createdJobCardId;
    private String createdJobCardNo;
}
