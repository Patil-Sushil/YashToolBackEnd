package com.kalibyte.YashTools.report.gst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gstr3bReportDTO {
    private String periodLabel;
    
    // Outward Supplies (Sales)
    private BigDecimal outwardTaxableValue;
    private BigDecimal outwardCgst;
    private BigDecimal outwardSgst;
    private BigDecimal outwardIgst;
    private BigDecimal totalOutwardTax;

    // Inward Supplies (Purchases ITC)
    private BigDecimal inwardTaxableValue;
    private BigDecimal inwardCgst;
    private BigDecimal inwardSgst;
    private BigDecimal inwardIgst;
    private BigDecimal totalInwardItc;

    // Net Tax Payable
    private BigDecimal netCgstPayable;
    private BigDecimal netSgstPayable;
    private BigDecimal netIgstPayable;
    private BigDecimal totalNetPayable;
}
