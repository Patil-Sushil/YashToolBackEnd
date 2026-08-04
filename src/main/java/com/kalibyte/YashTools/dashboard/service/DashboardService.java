package com.kalibyte.YashTools.dashboard.service;

import com.kalibyte.YashTools.dashboard.dto.*;

public interface DashboardService {
    AdminDashboardResponse getAdminDashboard();
    SupervisorDashboardResponse getSupervisorDashboard();
    ManagerDashboardResponse getManagerDashboard();
    SalesDashboardResponse getSalesDashboard();
    ProductionDashboardResponse getProductionDashboard();
    StoreDashboardResponse getStoreDashboard();
}
