package com.kalibyte.YashTools.production.logistics.damage.service;

import com.kalibyte.YashTools.production.logistics.damage.dto.CreateTransitDamageRequest;
import com.kalibyte.YashTools.production.logistics.damage.dto.TransitDamageResponse;

import java.util.List;
import java.util.UUID;

public interface TransitDamageService {
    TransitDamageResponse reportTransitDamage(CreateTransitDamageRequest request, String username);
    TransitDamageResponse approveReport(UUID reportId, String username);
    TransitDamageResponse rejectReport(UUID reportId, String username);
    List<TransitDamageResponse> getDamageReportsForChallan(UUID challanId);
    TransitDamageResponse getReportById(UUID id);
}
