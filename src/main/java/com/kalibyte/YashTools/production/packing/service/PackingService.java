package com.kalibyte.YashTools.production.packing.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.production.packing.dto.PackingRequest;
import com.kalibyte.YashTools.production.packing.dto.PackingResponse;

import java.util.List;
import java.util.UUID;

public interface PackingService {
    PackingResponse recordPacking(PackingRequest request);
    PackingResponse getPackingById(UUID id);
    List<PackingResponse> getPackingByWorkOrderItem(UUID workOrderItemId);
    PageResponse<PackingResponse> getAllPackingLogs(int page, int size);
}
