package com.kalibyte.YashTools.production.tracking.service;

import com.kalibyte.YashTools.production.tracking.dto.PlannerDashboardResponse;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionRequest;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionResponse;

import java.util.UUID;

public interface QualityInspectionService {
    QualityInspectionResponse inspect(QualityInspectionRequest request);
    QualityInspectionResponse getById(UUID id);
    QualityInspectionResponse getByJobCardId(UUID jobCardId);
    PlannerDashboardResponse getDashboard();
}
