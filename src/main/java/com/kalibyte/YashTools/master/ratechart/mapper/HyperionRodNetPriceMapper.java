package com.kalibyte.YashTools.master.ratechart.mapper;

import com.kalibyte.YashTools.master.ratechart.dto.request.HyperionRodNetPriceRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.HyperionRodNetPriceResponse;
import com.kalibyte.YashTools.master.ratechart.entity.HyperionRodNetPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface HyperionRodNetPriceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    HyperionRodNetPrice toEntity(HyperionRodNetPriceRequest request);

    HyperionRodNetPriceResponse toResponse(HyperionRodNetPrice entity);

    List<HyperionRodNetPriceResponse> toResponseList(List<HyperionRodNetPrice> entities);
}
