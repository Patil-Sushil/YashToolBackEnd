package com.kalibyte.YashTools.purchase.transaction.vendorpayment.mapper;

import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentItemRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.request.VendorPaymentRequest;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentItemResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.dto.response.VendorPaymentResponse;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPayment;
import com.kalibyte.YashTools.purchase.transaction.vendorpayment.entity.VendorPaymentItem;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface VendorPaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "company", ignore = true)
    VendorPayment toEntity(VendorPaymentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendorPayment", ignore = true)
    @Mapping(target = "purchaseInvoice", ignore = true)
    VendorPaymentItem toItemEntity(VendorPaymentItemRequest request);

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.vendorName")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    VendorPaymentResponse toResponse(VendorPayment entity);

    @Mapping(target = "purchaseInvoiceId", source = "purchaseInvoice.id")
    VendorPaymentItemResponse toItemResponse(VendorPaymentItem entity);
}
