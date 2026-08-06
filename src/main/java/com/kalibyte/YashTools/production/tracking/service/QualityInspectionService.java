package com.kalibyte.YashTools.production.tracking.service;

import com.kalibyte.YashTools.production.tracking.dto.PlannerDashboardResponse;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionRequest;
import com.kalibyte.YashTools.production.tracking.dto.QualityInspectionResponse;

import java.util.List;
import java.util.UUID;

public interface QualityInspectionService {
    QualityInspectionResponse inspect(QualityInspectionRequest request);
    QualityInspectionResponse getById(UUID id);
    QualityInspectionResponse getByJobCardId(UUID jobCardId);
    List<QualityInspectionResponse> getAll();
    PlannerDashboardResponse getDashboard();
}

