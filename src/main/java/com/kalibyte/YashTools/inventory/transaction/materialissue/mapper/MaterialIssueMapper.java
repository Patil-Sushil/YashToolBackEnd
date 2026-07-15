package com.kalibyte.YashTools.inventory.transaction.materialissue.mapper;

import com.kalibyte.YashTools.inventory.transaction.materialissue.dto.MaterialIssueResponse;
import com.kalibyte.YashTools.inventory.transaction.materialissue.entity.MaterialIssue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MaterialIssueMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    @Mapping(target = "cutPieceId", source = "cutPiece.id")
    @Mapping(target = "cutPieceCode", source = "cutPiece.code")
    @Mapping(target = "newCutPieceId", source = "newCutPiece.id")
    @Mapping(target = "newCutPieceCode", source = "newCutPiece.code")
    MaterialIssueResponse toResponse(MaterialIssue materialIssue);
}
