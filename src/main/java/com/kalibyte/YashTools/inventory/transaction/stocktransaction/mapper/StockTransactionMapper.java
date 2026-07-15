package com.kalibyte.YashTools.inventory.transaction.stocktransaction.mapper;

import com.kalibyte.YashTools.inventory.transaction.stocktransaction.dto.StockTransactionResponse;
import com.kalibyte.YashTools.inventory.transaction.stocktransaction.entity.StockTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface StockTransactionMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    StockTransactionResponse toResponse(StockTransaction transaction);
}
