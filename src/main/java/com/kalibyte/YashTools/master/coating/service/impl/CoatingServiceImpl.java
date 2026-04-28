package com.kalibyte.YashTools.master.coating.service.impl;


import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.master.coating.dto.CoatingRequest;
import com.kalibyte.YashTools.master.coating.dto.CoatingResponse;
import com.kalibyte.YashTools.master.coating.entity.Coating;
import com.kalibyte.YashTools.master.coating.mapper.CoatingMapper;
import com.kalibyte.YashTools.master.coating.repository.CoatingRepository;
import com.kalibyte.YashTools.master.coating.service.CoatingService;
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
