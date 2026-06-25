package com.kalibyte.YashTools.master.rawmaterial.service;

import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;

import java.util.List;
import java.util.UUID;

public interface RawMaterialService {

    RawMaterialResponse create(RawMaterialRequest request);

    RawMaterialResponse update(UUID id, RawMaterialRequest request);

    List<RawMaterialResponse> getAllActive();

    RawMaterialResponse getById(UUID id);

    void deactivate(UUID id);
}