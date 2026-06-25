package com.kalibyte.YashTools.master.ratechart.service;

import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionRodNetPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionRodNetPriceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface HyperionRodNetPriceService {
    HyperionRodNetPriceResponse create(HyperionRodNetPriceRequest request);
    HyperionRodNetPriceResponse update(UUID id, HyperionRodNetPriceRequest request);
    List<HyperionRodNetPriceResponse> getAllActive();
    Page<HyperionRodNetPriceResponse> getAllActivePaginated(Pageable pageable);
    HyperionRodNetPriceResponse getById(UUID id);
    void deactivate(UUID id);
    int importExcel(InputStream inputStream);
}