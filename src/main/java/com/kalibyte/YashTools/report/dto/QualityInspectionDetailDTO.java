package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionDetailDTO {
    private UUID inspectionId;
    private String jobCardNo;
    private LocalDateTime inspectionDate;
    private String inspector;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer reworkQuantity;
    private String result;
    private String remarks;
}
