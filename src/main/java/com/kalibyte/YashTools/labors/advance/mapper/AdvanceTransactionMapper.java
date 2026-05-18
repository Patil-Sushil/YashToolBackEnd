package com.kalibyte.YashTools.labors.advance.mapper;

import com.kalibyte.YashTools.labors.advance.dto.AdvanceTransactionRequestDTO;
import com.kalibyte.YashTools.labors.advance.dto.AdvanceTransactionResponseDTO;
import com.kalibyte.YashTools.labors.advance.entity.AdvanceTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdvanceTransactionMapper {
	AdvanceTransaction toEntity(AdvanceTransactionRequestDTO requestDTO);

	AdvanceTransactionResponseDTO toResponseDTO(AdvanceTransaction entity);
}
