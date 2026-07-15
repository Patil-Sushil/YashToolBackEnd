package com.kalibyte.YashTools.inventory.transaction.cutpiece.mapper;

import com.kalibyte.YashTools.inventory.transaction.cutpiece.dto.CutPieceResponse;
import com.kalibyte.YashTools.inventory.transaction.cutpiece.entity.CutPiece;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CutPieceMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    CutPieceResponse toResponse(CutPiece cutPiece);
}
