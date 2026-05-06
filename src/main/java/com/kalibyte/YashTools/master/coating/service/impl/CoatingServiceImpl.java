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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoatingServiceImpl implements CoatingService {

    private final CoatingRepository repository;
    private final CoatingMapper coatingMapper;

    public CoatingServiceImpl(CoatingRepository repository, CoatingMapper coatingMapper) {
        this.repository = repository;
        this.coatingMapper = coatingMapper;
    }

    @Override
    @Transactional
    public CoatingResponse create(CoatingRequest request) {
        Coating coating = coatingMapper.toEntity(request);
        return coatingMapper.toResponse(repository.save(coating));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoatingResponse> getAllActive() {
        return coatingMapper.toResponseList(repository.findByActiveTrue());
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Coating coating = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coating not found"));
        coating.setActive(false);
        repository.save(coating);

    }
}
