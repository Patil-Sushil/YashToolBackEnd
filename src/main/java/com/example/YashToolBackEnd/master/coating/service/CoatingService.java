package com.example.YashToolBackEnd.master.coating.service;

import com.example.YashToolBackEnd.master.coating.dto.CoatingRequest;
import com.example.YashToolBackEnd.master.coating.dto.CoatingResponse;

import java.util.List;

public interface CoatingService {
    CoatingResponse create(CoatingRequest request);

    List<CoatingResponse> getAllActive();

    void deactivate(Long id);
}
