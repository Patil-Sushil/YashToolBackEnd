package com.example.YashToolBackEnd.master.rawmaterial.service;

import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialRequest;
import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialResponse;

import java.util.List;

public interface RawMaterialService {
    RawMaterialResponse create(RawMaterialRequest request);

    List<RawMaterialResponse> getAllActive();

    void deactivate(Long id);
}
