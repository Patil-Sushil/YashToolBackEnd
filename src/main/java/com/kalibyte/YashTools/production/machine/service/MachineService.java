package com.kalibyte.YashTools.production.machine.service;

import com.kalibyte.YashTools.production.machine.dto.MachineRequest;
import com.kalibyte.YashTools.production.machine.dto.MachineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MachineService {
    MachineResponse create(MachineRequest request);
    MachineResponse update(UUID id, MachineRequest request);
    MachineResponse getById(UUID id);
    Page<MachineResponse> list(Pageable pageable);
    void delete(UUID id);
}
