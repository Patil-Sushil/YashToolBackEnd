package com.kalibyte.YashTools.workorder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderProgressResponse {
    private UUID workOrderId;
    private String workOrderNo;
    private String status;
    private String customerCompanyName;
    private LocalDate expectedDeliveryDate;
    private List<WorkOrderItemProgressResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkOrderItemProgressResponse {
        private UUID itemId;
        private String toolName;
        private String itemName;
        private Integer orderedQuantity;
        private Double diameter;
        private Double shankDiameter;
        private Double overallLength;
        private Double fluteLength;
        private String drawingReference;
        private String materialGrade;
        private String materialType;
        private String coatingType;
        private String technicalNotes;
        private Boolean trial;
        private String trialStatus;
        private Integer plannedQuantity;
        private Integer producedQuantity;
        private Integer acceptedQuantity;
        private Integer rejectedQuantity;
        private Integer reworkQuantity;
        private Integer packedQuantity;
        private Integer deliveredQuantity;
        private List<JobCardSummaryResponse> jobCards;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobCardSummaryResponse {
        private UUID jobCardId;
        private String jobCardNo;
        private String status;
        private Boolean isRework;
        private Integer quantity;
    }
}
