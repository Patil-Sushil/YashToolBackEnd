package com.kalibyte.YashTools.master.rawmaterial.service;

import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;

import java.util.List;

public interface RawMaterialService {
    RawMaterialResponse create(RawMaterialRequest request);

    List<RawMaterialResponse> getAllActive();

    void deactivate(Long id);
}
