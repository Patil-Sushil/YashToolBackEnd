package com.kalibyte.YashTools.report.gst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gstr1ReportDTO {
    private String periodLabel;
    private BigDecimal totalTaxableValue;
    private BigDecimal totalCgstAmount;
    private BigDecimal totalSgstAmount;
    private BigDecimal totalIgstAmount;
    private BigDecimal totalGstAmount;
    private BigDecimal totalInvoiceValue;
    private List<Gstr1ItemDTO> items;
}
