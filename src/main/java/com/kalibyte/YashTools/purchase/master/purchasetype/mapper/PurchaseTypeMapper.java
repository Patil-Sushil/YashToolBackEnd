package com.kalibyte.YashTools.purchase.master.purchasetype.mapper;

import com.kalibyte.YashTools.purchase.master.purchasetype.dto.request.PurchaseTypeRequest;
import com.kalibyte.YashTools.purchase.master.purchasetype.dto.response.PurchaseTypeResponse;
import com.kalibyte.YashTools.purchase.master.purchasetype.entity.PurchaseType;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    PurchaseType toEntity(PurchaseTypeRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    PurchaseTypeResponse toResponse(PurchaseType entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntityFromRequest(PurchaseTypeRequest request, @MappingTarget PurchaseType entity);
}
