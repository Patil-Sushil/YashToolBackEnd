package com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.mapper;

import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceItemRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.request.PurchaseInvoiceRequest;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceItemResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.dto.response.PurchaseInvoiceResponse;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoice;
import com.kalibyte.YashTools.purchase.transaction.purchaseinvoice.entity.PurchaseInvoiceItem;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseInvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "goodsReceipts", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    PurchaseInvoice toEntity(PurchaseInvoiceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseInvoice", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    @Mapping(target = "taxableAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    PurchaseInvoiceItem toItemEntity(PurchaseInvoiceItemRequest request);

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.vendorName")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "goodsReceiptIds", ignore = true)
    @Mapping(target = "goodsReceiptNumbers", ignore = true)
    @Mapping(target = "warnings", ignore = true)
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    PurchaseInvoiceResponse toResponse(PurchaseInvoice entity);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    PurchaseInvoiceItemResponse toItemResponse(PurchaseInvoiceItem entity);
}
