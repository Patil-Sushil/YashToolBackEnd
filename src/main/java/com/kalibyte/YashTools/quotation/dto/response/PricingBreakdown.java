package com.kalibyte.YashTools.quotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingBreakdown {

    private BigDecimal ratePerUnit;
    private String rateChartItem;
    private double standardRodLengthMm;
    private Double actualLengthMm;
    private BigDecimal userMultiplier;
    private BigDecimal basePrice;
    private BigDecimal multipliedPrice;
    private BigDecimal coatingCharge;
    private BigDecimal unitPrice;
    private BigDecimal lineSubtotal;
    private String rateSourceTable;
    private String rateRecordId;
}