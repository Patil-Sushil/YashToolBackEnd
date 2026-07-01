package com.kalibyte.YashTools.quotation.dto.response;

import com.kalibyte.YashTools.common.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemResponse {

    private UUID itemId;
    private Integer lineNumber;
    private OrderType orderType;
    private String toolName;
    private String itemName;
    private Integer quantity;
    private Boolean trial;
    private String remarks;

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

    private String rateChartItem;
    private String rateChartGrade;
    private BigDecimal ratePerUnit;
    private Double standardRodLengthMm;
    private Double actualLengthUsed;
    private BigDecimal userMultiplier;
    private BigDecimal basePrice;
    private BigDecimal multipliedPrice;
    private BigDecimal coatingCharge;
    private BigDecimal unitPrice;
    private BigDecimal lineSubtotal;
    private BigDecimal lineDiscountAmount;
    private BigDecimal lineTotal;
}