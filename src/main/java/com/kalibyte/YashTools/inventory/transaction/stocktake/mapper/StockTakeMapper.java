package com.kalibyte.YashTools.inventory.transaction.stocktake.mapper;

import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeLineResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.dto.StockTakeResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTake;
import com.kalibyte.YashTools.inventory.transaction.stocktake.entity.StockTakeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface StockTakeMapper {

    StockTakeResponse toResponse(StockTake stockTake);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    StockTakeLineResponse toLineResponse(StockTakeLine line);
}
