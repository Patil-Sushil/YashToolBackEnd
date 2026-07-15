package com.kalibyte.YashTools.inventory.master.item.mapper;

import com.kalibyte.YashTools.inventory.master.item.dto.ItemRequest;
import com.kalibyte.YashTools.inventory.master.item.dto.ItemResponse;
import com.kalibyte.YashTools.inventory.master.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Item toEntity(ItemRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    ItemResponse toResponse(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(ItemRequest request, @MappingTarget Item item);
}
