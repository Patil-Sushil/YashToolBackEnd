package com.kalibyte.YashTools.inventory.master.materialgrade.mapper;

import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeRequest;
import com.kalibyte.YashTools.inventory.master.materialgrade.dto.MaterialGradeResponse;
import com.kalibyte.YashTools.inventory.master.materialgrade.entity.MaterialGrade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MaterialGradeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    MaterialGrade toEntity(MaterialGradeRequest request);

    MaterialGradeResponse toResponse(MaterialGrade materialGrade);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(MaterialGradeRequest request, @MappingTarget MaterialGrade materialGrade);
}
