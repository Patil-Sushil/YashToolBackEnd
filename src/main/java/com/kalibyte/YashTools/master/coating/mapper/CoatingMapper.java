package com.kalibyte.YashTools.master.coating.mapper;

import com.kalibyte.YashTools.master.coating.dto.CoatingRequest;
import com.kalibyte.YashTools.master.coating.dto.CoatingResponse;
import com.kalibyte.YashTools.master.coating.entity.Coating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CoatingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Coating toEntity(CoatingRequest request);

    @Mapping(target = "rate", expression = "java(coating.getRate() == null ? null : String.valueOf(coating.getRate()))")
    CoatingResponse toResponse(Coating coating);

    @Mapping(target = "rate", expression = "java(coating.getRate() == null ? null : String.valueOf(coating.getRate()))")
    List<CoatingResponse> toResponseList(List<Coating> coatings);
}
