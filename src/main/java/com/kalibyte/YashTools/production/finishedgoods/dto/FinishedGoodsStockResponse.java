package com.kalibyte.YashTools.production.finishedgoods.dto;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinishedGoodsStockResponse {
    private UUID id;
    private UUID workOrderItemId;
    private String workOrderNo;
    private String toolName;
    private String itemName;
    private Integer quantity;
    private Double diameter;
    private Double shankDiameter;
    private Double overallLength;
    private Double fluteLength;
    private String drawingReference;
    private String materialGrade;
    private String materialType;
    private String coatingType;
    private String technicalNotes;
    private UUID companyId;
}
