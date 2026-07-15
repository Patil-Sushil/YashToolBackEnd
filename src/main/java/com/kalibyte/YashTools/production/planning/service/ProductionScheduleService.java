package com.kalibyte.YashTools.production.planning.service;

import com.kalibyte.YashTools.production.planning.dto.ScheduleRequest;
import com.kalibyte.YashTools.production.planning.dto.ScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductionScheduleService {
    ScheduleResponse schedule(ScheduleRequest request);
    ScheduleResponse reschedule(UUID id, ScheduleRequest request);
    ScheduleResponse getById(UUID id);
    ScheduleResponse getByJobCardId(UUID jobCardId);
    Page<ScheduleResponse> list(Pageable pageable);
}
