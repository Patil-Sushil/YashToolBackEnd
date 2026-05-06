package com.kalibyte.YashTools.master.rawmaterial.service.impl;

import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;
import com.kalibyte.YashTools.master.rawmaterial.entity.RawMaterial;
import com.kalibyte.YashTools.master.rawmaterial.mapper.RawMaterialMapper;
import com.kalibyte.YashTools.master.rawmaterial.repository.RawMaterialRepository;
import com.kalibyte.YashTools.master.rawmaterial.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RawMaterialServiceImpl implements RawMaterialService {

    private final RawMaterialRepository repository;
    private final RawMaterialMapper rawMaterialMapper;

    public RawMaterialServiceImpl(RawMaterialRepository repository, RawMaterialMapper rawMaterialMapper) {
        this.repository = repository;
        this.rawMaterialMapper = rawMaterialMapper;
    }


    @Override
    @Transactional
    public RawMaterialResponse create(RawMaterialRequest request) {
        RawMaterial material = rawMaterialMapper.toEntity(request);
        return rawMaterialMapper.toResponse(repository.save(material));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawMaterialResponse> getAllActive() {
        return rawMaterialMapper.toResponseList(repository.findByActiveTrue());
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        RawMaterial material = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material not found with id: " + id));
        material.setActive(false);
        repository.save(material);

    }
}
