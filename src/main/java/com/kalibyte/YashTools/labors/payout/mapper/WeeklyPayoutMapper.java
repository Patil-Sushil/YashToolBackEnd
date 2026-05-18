package com.kalibyte.YashTools.labors.payout.mapper;

import com.kalibyte.YashTools.labors.payout.dto.WeeklyPayoutResponseDTO;
import com.kalibyte.YashTools.labors.payout.entity.WeeklyPayout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeeklyPayoutMapper {

    @Mapping(target = "laborerId", source = "laborer.id")
    @Mapping(target = "laborerName", source = "laborer.name")
    WeeklyPayoutResponseDTO toResponse(WeeklyPayout entity);
}
