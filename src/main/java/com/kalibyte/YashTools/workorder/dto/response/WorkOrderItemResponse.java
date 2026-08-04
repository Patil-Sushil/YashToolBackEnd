package com.kalibyte.YashTools.workorder.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderItemResponse {
    private UUID id;
    private Integer lineNumber;
    private UUID quotationItemId;
    private String orderType;
    private String toolName;
    private String itemName;
    private Integer quantity;
    private Boolean trial;
    private String trialStatus;
    private String trialFeedback;
    private String itemRemarks;
    private String drawingReference;
    private String materialType;
    private String materialGrade;
    private Boolean coatingRequired;
    private String coatingType;
    private String resharpeningType;
    private Double diameter;
    private Double fluteLength;
    private Double shankDiameter;
    private Double overallLength;
    private String technicalNotes;
    private String damageLevel;
    private Boolean specialGeometry;
    private Boolean specialProfile;
    private Boolean expressDelivery;
}
