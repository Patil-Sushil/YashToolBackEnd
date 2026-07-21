package com.kalibyte.YashTools.production.tracking.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionResponse {
    private UUID id;
    private UUID jobCardId;
    private String jobCardNo;
    private Integer totalQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer reworkQuantity;
    private String inspector;
    private LocalDateTime inspectionDate;
    private String result;
    private String remarks;
    private UUID companyId;
}
