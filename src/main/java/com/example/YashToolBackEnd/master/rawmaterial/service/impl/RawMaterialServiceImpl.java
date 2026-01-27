package com.example.YashToolBackEnd.master.rawmaterial.service.impl;

import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialRequest;
import com.example.YashToolBackEnd.master.rawmaterial.dto.RawMaterialResponse;
import com.example.YashToolBackEnd.master.rawmaterial.entity.RawMaterial;
import com.example.YashToolBackEnd.master.rawmaterial.mapper.RawMaterialMapper;
import com.example.YashToolBackEnd.master.rawmaterial.repository.RawMaterialRepository;
import com.example.YashToolBackEnd.master.rawmaterial.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RawMaterialServiceImpl implements RawMaterialService {

    private final RawMaterialRepository repository;


    @Override
    public RawMaterialResponse create(RawMaterialRequest request) {
        RawMaterial material = RawMaterialMapper.toEntity(request);
        return RawMaterialMapper.toResponse(repository.save(material));
    }

    @Override
    public List<RawMaterialResponse> getAllActive() {
        return repository.findAll()
                .stream()
                .filter(RawMaterial::getActive)
                .map(RawMaterialMapper::toResponse)
                .toList();
    }

    @Override
    public void deactivate(Long id) {
        RawMaterial material = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material not found with id: " + id));
        material.setActive(false);
        repository.save(material);

    }
}
