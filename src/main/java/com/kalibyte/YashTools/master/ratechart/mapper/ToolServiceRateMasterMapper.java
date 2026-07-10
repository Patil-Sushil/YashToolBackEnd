package com.kalibyte.YashTools.master.ratechart.mapper;

import com.kalibyte.YashTools.master.ratechart.dto.request.ToolServiceRateMasterRequest;
import com.kalibyte.YashTools.master.ratechart.dto.response.ToolServiceRateMasterResponse;
import com.kalibyte.YashTools.master.ratechart.entity.ToolServiceRateMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ToolServiceRateMasterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ToolServiceRateMaster toEntity(ToolServiceRateMasterRequest request);

    @Mapping(target = "companyId", source = "company.id")
    ToolServiceRateMasterResponse toResponse(ToolServiceRateMaster entity);

    List<ToolServiceRateMasterResponse> toResponseList(List<ToolServiceRateMaster> entities);
}
