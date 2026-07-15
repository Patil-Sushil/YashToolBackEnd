package com.kalibyte.YashTools.inventory.master.materialgrade.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.exception.ResourceNotFoundException;
import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeRequest;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import com.kalibyte.YashTools.inventory.master.materialgrade.mapper.MaterialGradeMapper;
import com.kalibyte.YashTools.inventory.master.materialgrade.repository.MaterialGradeRepository;
import com.kalibyte.YashTools.inventory.master.materialgrade.service.MaterialGradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialGradeServiceImpl implements MaterialGradeService {

    private final MaterialGradeRepository materialGradeRepository;
    private final MaterialGradeMapper materialGradeMapper;

    public MaterialGradeServiceImpl(MaterialGradeRepository materialGradeRepository, MaterialGradeMapper materialGradeMapper) {
        this.materialGradeRepository = materialGradeRepository;
        this.materialGradeMapper = materialGradeMapper;
    }

    @Override
    @Transactional
    public MaterialGradeResponse createMaterialGrade(MaterialGradeRequest request) {
        materialGradeRepository.findByCode(request.getCode().trim())
                .ifPresent(m -> {
                    throw new BusinessException("Material grade code already exists: " + request.getCode());
                });

        materialGradeRepository.findByName(request.getName().trim())
                .ifPresent(m -> {
                    throw new BusinessException("Material grade name already exists: " + request.getName());
                });

        MaterialGrade materialGrade = materialGradeMapper.toEntity(request);
        materialGrade.setCode(materialGrade.getCode().trim());
        materialGrade.setName(materialGrade.getName().trim());

        return materialGradeMapper.toResponse(materialGradeRepository.save(materialGrade));
    }

    @Override
    @Transactional
    public MaterialGradeResponse updateMaterialGrade(UUID id, MaterialGradeRequest request) {
        MaterialGrade materialGrade = materialGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material grade not found with ID: " + id));

        materialGradeRepository.findByCode(request.getCode().trim())
                .filter(m -> !m.getId().equals(id))
                .ifPresent(m -> {
                    throw new BusinessException("Material grade code already exists: " + request.getCode());
                });

        materialGradeRepository.findByName(request.getName().trim())
                .filter(m -> !m.getId().equals(id))
                .ifPresent(m -> {
                    throw new BusinessException("Material grade name already exists: " + request.getName());
                });

        materialGradeMapper.updateEntityFromRequest(request, materialGrade);
        materialGrade.setCode(materialGrade.getCode().trim());
        materialGrade.setName(materialGrade.getName().trim());

        return materialGradeMapper.toResponse(materialGradeRepository.save(materialGrade));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialGradeResponse getMaterialGradeById(UUID id) {
        MaterialGrade materialGrade = materialGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material grade not found with ID: " + id));
        return materialGradeMapper.toResponse(materialGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialGradeResponse> getAllMaterialGrades() {
        return materialGradeRepository.findAll().stream()
                .map(materialGradeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MaterialGradeResponse> getAllMaterialGrades(int page, int size) {
        Page<MaterialGrade> materialGradePage = materialGradeRepository.findAll(PageRequest.of(page, size));
        return PageResponse.from(materialGradePage, materialGradeMapper::toResponse);
    }
}
