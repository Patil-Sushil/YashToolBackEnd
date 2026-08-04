package com.kalibyte.YashTools.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoatingUtilizationDetailDTO {
    private String coatingType;
    private String coatingName;
    private long jobCardCount;
    private Integer coatedQuantity;
    private BigDecimal rate;
    private BigDecimal estimatedCost;
}
