package com.kalibyte.YashTools.master.rawmaterial.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;
import com.kalibyte.YashTools.master.rawmaterial.entity.RawMaterial;
import com.kalibyte.YashTools.master.rawmaterial.mapper.RawMaterialMapper;
import com.kalibyte.YashTools.master.rawmaterial.repository.RawMaterialRepository;
import com.kalibyte.YashTools.master.rawmaterial.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RawMaterialServiceImpl implements RawMaterialService {

    private final RawMaterialRepository repository;
    private final RawMaterialMapper mapper;

    @Override
    public RawMaterialResponse create(RawMaterialRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Raw material already exists");
        }

        RawMaterial material = mapper.toEntity(request);

        return mapper.toResponse(
                repository.save(material)
        );
    }

    @Override
    public RawMaterialResponse update(UUID id, RawMaterialRequest request) {

        RawMaterial material = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Raw material not found"));

        material.setName(request.getName());
        material.setRate(request.getRate());

        return mapper.toResponse(
                repository.save(material)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getAllActive() {
        return mapper.toResponseList(
                repository.findByActiveTrue()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RawMaterialResponse getById(UUID id) {

        RawMaterial material = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Raw material not found"));

        return mapper.toResponse(material);
    }

    @Override
    public void deactivate(UUID id) {

        RawMaterial material = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Raw material not found"));

        material.setActive(false);
        material.setDeactivatedAt(LocalDateTime.now());

        repository.save(material);
    }
}