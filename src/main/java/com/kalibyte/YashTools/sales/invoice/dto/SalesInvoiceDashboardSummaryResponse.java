package com.kalibyte.YashTools.sales.invoice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDashboardSummaryResponse {
    private BigDecimal grossSalesInvoiced;
    private long totalInvoicesCount;
    private BigDecimal collectionsSettled;
    private long paidInvoicesCount;
    private BigDecimal outstandingReceivables;
    private long pendingInvoicesCount;
}
