package com.kalibyte.YashTools.master.rawmaterial.mapper;

import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialRequest;
import com.kalibyte.YashTools.master.rawmaterial.dto.RawMaterialResponse;
import com.kalibyte.YashTools.master.rawmaterial.entity.RawMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface RawMaterialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    RawMaterial toEntity(RawMaterialRequest request);

    RawMaterialResponse toResponse(RawMaterial material);

    List<RawMaterialResponse> toResponseList(List<RawMaterial> entities);

}
