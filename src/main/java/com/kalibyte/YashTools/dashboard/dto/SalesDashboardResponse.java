package com.kalibyte.YashTools.dashboard.dto;

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
public class SalesDashboardResponse {
    private long totalEnquiries;
    private long openEnquiries;
    private long closedEnquiries;
    private long totalQuotations;
    private long approvedQuotations;
    private double quotationConversionRate;
    private BigDecimal salesInvoiceRevenue;
    private List<CustomerSalesDto> topCustomers;
}
