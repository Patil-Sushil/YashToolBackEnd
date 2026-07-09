package com.kalibyte.YashTools.quotation.mapper;

import com.kalibyte.YashTools.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    @Mapping(target = "quotationId", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "sourceEnquiryId", source = "sourceEnquiry.id")
    @Mapping(target = "parentQuotationId", source = "parentQuotation.id")
    @Mapping(target = "customerCompanyName", source = "customer.companyName")
    @Mapping(target = "customerContactPerson", source = "customer.customerName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerMobile", source = "customer.mobileNumber")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    QuotationResponse toResponse(Quotation quotation);

    @Named("mapItems")
    default List<QuotationItemResponse> mapItems(List<QuotationItem> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Mapping(target = "itemId", source = "id")
    QuotationItemResponse toItemResponse(QuotationItem item);
}