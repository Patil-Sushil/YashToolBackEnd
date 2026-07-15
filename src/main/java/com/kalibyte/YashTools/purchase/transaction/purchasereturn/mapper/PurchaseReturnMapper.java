package com.kalibyte.YashTools.purchase.transaction.purchasereturn.mapper;

import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request.PurchaseReturnItemRequest;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.request.PurchaseReturnRequest;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response.PurchaseReturnItemResponse;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.dto.response.PurchaseReturnResponse;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.entity.PurchaseReturn;
import com.kalibyte.YashTools.purchase.transaction.purchasereturn.entity.PurchaseReturnItem;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface PurchaseReturnMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "purchaseInvoice", ignore = true)
    @Mapping(target = "goodsReceipt", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    PurchaseReturn toEntity(PurchaseReturnRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseReturn", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "materialGrade", ignore = true)
    PurchaseReturnItem toItemEntity(PurchaseReturnItemRequest request);

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.vendorName")
    @Mapping(target = "purchaseInvoiceId", source = "purchaseInvoice.id")
    @Mapping(target = "purchaseInvoiceNumber", source = "purchaseInvoice.invoiceNumber")
    @Mapping(target = "goodsReceiptId", source = "goodsReceipt.id")
    @Mapping(target = "grnNumber", source = "goodsReceipt.grnNumber")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    PurchaseReturnResponse toResponse(PurchaseReturn entity);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemSku", source = "item.sku")
    @Mapping(target = "materialGradeId", source = "materialGrade.id")
    @Mapping(target = "materialGradeName", source = "materialGrade.name")
    @Mapping(target = "materialGradeCode", source = "materialGrade.code")
    PurchaseReturnItemResponse toItemResponse(PurchaseReturnItem entity);
}
