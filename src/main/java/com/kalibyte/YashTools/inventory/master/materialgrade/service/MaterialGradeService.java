package com.kalibyte.YashTools.inventory.master.materialgrade.service;

import com.kalibyte.YashTools.common.response.PageResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeRequest;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeResponse;

import java.util.List;
import java.util.UUID;

public interface MaterialGradeService {
    MaterialGradeResponse createMaterialGrade(MaterialGradeRequest request);
    MaterialGradeResponse updateMaterialGrade(UUID id, MaterialGradeRequest request);
    MaterialGradeResponse getMaterialGradeById(UUID id);
    List<MaterialGradeResponse> getAllMaterialGrades();
    PageResponse<MaterialGradeResponse> getAllMaterialGrades(int page, int size);
}
