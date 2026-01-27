package com.example.YashToolBackEnd.master.coating.service.impl;


import com.example.YashToolBackEnd.common.exception.ResourceNotFoundException;
import com.example.YashToolBackEnd.master.coating.dto.CoatingRequest;
import com.example.YashToolBackEnd.master.coating.dto.CoatingResponse;
import com.example.YashToolBackEnd.master.coating.entity.Coating;
import com.example.YashToolBackEnd.master.coating.mapper.CoatingMapper;
import com.example.YashToolBackEnd.master.coating.repository.CoatingRepository;
import com.example.YashToolBackEnd.master.coating.service.CoatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoatingServiceImpl implements CoatingService {

    private final CoatingRepository repository;

    @Override
    public CoatingResponse create(CoatingRequest request) {
        Coating coating = CoatingMapper.toEntity(request);
        return CoatingMapper.toResponse(repository.save(coating));
    }

    @Override
    public List<CoatingResponse> getAllActive() {
        return repository.findAll()
                .stream()
                .filter(Coating::getActive)
                .map(CoatingMapper::toResponse)
                .toList();
    }

    @Override
    public void deactivate(Long id) {
        Coating coating = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coating not found"));
        coating.setActive(false);
        repository.save(coating);

    }
}
