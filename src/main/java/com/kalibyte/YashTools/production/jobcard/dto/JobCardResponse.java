package com.kalibyte.YashTools.production.jobcard.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCardResponse {
    private UUID id;
    private String jobCardNo;
    private UUID workOrderId;
    private String workOrderNo;
    private UUID workOrderItemId;
    private Integer lineNumber;
    private String toolName;
    private String itemName;
    private String status;
    private Integer priority;
    private Integer totalQuantity;
    private String remarks;
    private Boolean isRework;
    private UUID reworkParentJobCardId;
    private String reworkParentJobCardNo;
    private LocalDateTime createdAt;
    private String createdBy;
    private UUID companyId;
}
