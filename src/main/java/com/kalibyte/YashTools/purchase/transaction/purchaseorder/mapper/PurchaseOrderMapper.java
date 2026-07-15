package com.kalibyte.YashTools.purchase.transaction.purchaseorder.mapper;

import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderItemRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.request.PurchaseOrderRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderItemResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.dto.response.PurchaseOrderResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.YashTools.purchase.transaction.purchaseorder.entity.PurchaseOrderItem;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "paymentTerms", ignore = true)
    @Mapping(target = "purchaseType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    PurchaseOrder toEntity(PurchaseOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    PurchaseOrderItem toItemEntity(PurchaseOrderItemRequest request);

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.vendorName")
    @Mapping(target = "paymentTermsId", source = "paymentTerms.id")
    @Mapping(target = "paymentTermsCode", source = "paymentTerms.code")
    @Mapping(target = "paymentTermsName", source = "paymentTerms.name")
    @Mapping(target = "purchaseTypeId", source = "purchaseType.id")
    @Mapping(target = "purchaseTypeCode", source = "purchaseType.code")
    @Mapping(target = "purchaseTypeName", source = "purchaseType.name")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    PurchaseOrderResponse toResponse(PurchaseOrder entity);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "paymentTerms", ignore = true)
    @Mapping(target = "purchaseType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntityFromRequest(PurchaseOrderRequest request, @MappingTarget PurchaseOrder entity);
}
