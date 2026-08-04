package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {
    private long pendingQuotationApprovals;
    private long workOrdersCompleted;
    private long workOrdersPending;
    private BigDecimal totalPurchaseInvoiceExpense;
    private BigDecimal totalSalesInvoiceRevenue;
    private long delayedJobsCount;
    private double averageMachineUtilization;
}
