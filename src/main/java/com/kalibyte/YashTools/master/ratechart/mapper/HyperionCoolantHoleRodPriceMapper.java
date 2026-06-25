package com.kalibyte.YashTools.master.ratechart.mapper;

import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionCoolantHoleRodPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionCoolantHoleRodPriceResponse;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionCoolantHoleRodPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface HyperionCoolantHoleRodPriceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    HyperionCoolantHoleRodPrice toEntity(HyperionCoolantHoleRodPriceRequest request);

    HyperionCoolantHoleRodPriceResponse toResponse(HyperionCoolantHoleRodPrice entity);

    List<HyperionCoolantHoleRodPriceResponse> toResponseList(List<HyperionCoolantHoleRodPrice> entities);
}
