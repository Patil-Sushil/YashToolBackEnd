package com.kalibyte.YashTools.master.coating.service;

import com.kalibyte.YashTools.master.coating.dto.CoatingRequest;
import com.kalibyte.YashTools.master.coating.dto.CoatingResponse;

import java.util.List;

public interface CoatingService {
    CoatingResponse create(CoatingRequest request);

    List<CoatingResponse> getAllActive();

    void deactivate(Long id);
}
