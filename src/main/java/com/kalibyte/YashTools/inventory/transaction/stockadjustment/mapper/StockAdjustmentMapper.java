package com.kalibyte.YashTools.inventory.transaction.stockadjustment.mapper;

import com.kalibyte.YashTools.inventory.transaction.stockadjustment.dto.StockAdjustmentResponse;
import com.kalibyte.YashTools.inventory.transaction.stockadjustment.entity.StockAdjustment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface StockAdjustmentMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    StockAdjustmentResponse toResponse(StockAdjustment adjustment);
}
