package com.kalibyte.YashTools.master.ratechart.service;

import com.kalibyte.YashTools.common.enums.ServiceType;
import com.kalibyte.YashTools.master.ratechart.dto.request.ToolServiceRateMasterRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.ToolServiceRateMasterResponse;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ToolServiceRateMasterService {

    ToolServiceRateMasterResponse create(ToolServiceRateMasterRequest request);

    ToolServiceRateMasterResponse update(UUID id, ToolServiceRateMasterRequest request);

    List<ToolServiceRateMasterResponse> getAllActive();

    Page<ToolServiceRateMasterResponse> getAllActivePaginated(Pageable pageable);

    ToolServiceRateMasterResponse getById(UUID id);

    void deactivate(UUID id);

    int importExcel(InputStream inputStream);

    ToolServiceRateMaster findMatchingRate(ServiceType serviceType, String toolType, String toolMaterial, Double diameter);
}
