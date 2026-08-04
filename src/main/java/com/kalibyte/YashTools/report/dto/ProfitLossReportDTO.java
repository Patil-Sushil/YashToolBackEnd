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
public class ProfitLossReportDTO {
    private String periodLabel;
    private BigDecimal revenue;             // Subtotal of non-cancelled sales invoices
    private BigDecimal taxRevenue;          // CGST + SGST + IGST of sales invoices
    private BigDecimal totalSalesValue;     // Subtotal + Taxes (sales invoices)
    private BigDecimal materialPurchaseCost;// Total of non-cancelled purchase invoices
    private BigDecimal laborCost;           // Total labor cost from attendance records
    private BigDecimal freightExpenses;     // Freight charges from purchase invoices
    private BigDecimal otherExpenses;       // Other charges from purchase invoices
    private BigDecimal totalExpenses;       // Material + Labor + Freight + Other
    private BigDecimal netProfit;           // Revenue - TotalExpenses
    private Double netProfitMargin;         // netProfit * 100 / revenue
}
