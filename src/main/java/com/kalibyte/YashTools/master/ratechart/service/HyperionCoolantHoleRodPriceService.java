package com.kalibyte.YashTools.master.ratechart.service;

import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionCoolantHoleRodPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionCoolantHoleRodPriceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface HyperionCoolantHoleRodPriceService {
    HyperionCoolantHoleRodPriceResponse create(HyperionCoolantHoleRodPriceRequest request);
    HyperionCoolantHoleRodPriceResponse update(UUID id, HyperionCoolantHoleRodPriceRequest request);
    List<HyperionCoolantHoleRodPriceResponse> getAllActive();
    Page<HyperionCoolantHoleRodPriceResponse> getAllActivePaginated(Pageable pageable);
    HyperionCoolantHoleRodPriceResponse getById(UUID id);
    void deactivate(UUID id);
    int importExcel(InputStream inputStream);
}