package com.kalibyte.YashTools.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalEnquiries;
    private long totalQuotations;
    private long totalWorkOrders;
    private BigDecimal totalInvoicedRevenue;
    private Map<String, Object> companyDetails;
    private List<AuditLogDto> recentAuditLogs;
}
