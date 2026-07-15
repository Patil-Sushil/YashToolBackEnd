package com.kalibyte.YashTools.purchase.transaction.goodsreceipt.mapper;

import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptItemRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.request.GoodsReceiptRequest;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptItemResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.dto.response.GoodsReceiptResponse;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceipt;
import com.kalibyte.YashTools.purchase.transaction.goodsreceipt.entity.GoodsReceiptItem;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface GoodsReceiptMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    GoodsReceipt toEntity(GoodsReceiptRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goodsReceipt", ignore = true)
    @Mapping(target = "poItemReference", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    @Mapping(target = "orderedQuantity", ignore = true)
    @Mapping(target = "previouslyReceivedQuantity", ignore = true)
    @Mapping(target = "currentReceivedQuantity", ignore = true)
    @Mapping(target = "pendingQuantity", ignore = true)
    GoodsReceiptItem toItemEntity(GoodsReceiptItemRequest request);

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.vendorName")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    GoodsReceiptResponse toResponse(GoodsReceipt entity);

    @Mapping(target = "poItemReferenceId", source = "poItemReference.id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    GoodsReceiptItemResponse toItemResponse(GoodsReceiptItem entity);
}
