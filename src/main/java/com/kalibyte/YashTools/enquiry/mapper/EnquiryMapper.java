package com.kalibyte.YashTools.enquiry.mapper;

import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.enquiry.dto.request.*;
import com.kalibyte.YashTools.enquiry.dto.response.*;
import com.kalibyte.YashTools.enquiry.entity.*;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface EnquiryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "remarks", source = "request.remarks")
    @Mapping(target = "isUrgent", source = "request.isUrgent", defaultValue = "false")
    @Mapping(target = "items", source = "request.items")
    Enquiry toEntity(CreateEnquiryRequest request, Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiry", ignore = true)
    @Mapping(target = "newToolSpecs", source = "newToolSpecs")
    @Mapping(target = "resharpeningSpecs", source = "resharpeningSpecs")
    @Mapping(target = "reformingSpecs", source = "reformingSpecs")
    EnquiryItem toItemEntity(EnquiryItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    @Mapping(target = "rawMaterial", ignore = true)
    @Mapping(target = "coating", ignore = true)
    @Mapping(target = "hasCoating", source = "hasCoating")
    NewToolSpecs toNewToolSpecs(NewToolSpecsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    @Mapping(target = "coating", ignore = true)
    @Mapping(target = "hasCoating", source = "hasCoating")
    ResharpeningSpecs toResharpeningSpecs(ResharpeningSpecsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    @Mapping(target = "coating", ignore = true)
    @Mapping(target = "hasCoating", source = "hasCoating")
    ReformingSpecs toReformingSpecs(ReformingSpecsRequest request);

    @Mapping(target = "enquiryId", source = "id")
    @Mapping(target = "customerName", source = "customer.companyName")
    EnquiryResponse toResponse(Enquiry enquiry);

    @Mapping(target = "itemId", source = "id")
    EnquiryItemResponse toItemResponse(EnquiryItem item);

    @Mapping(target = "coatingName", source = "coating.name")
    @Mapping(target = "rawMaterialName", source = "rawMaterial.name")
    NewToolSpecsResponse toNewToolSpecsResponse(NewToolSpecs specs);

    @Mapping(target = "coatingName", source = "coating.name")
    ResharpeningSpecsResponse toResharpeningSpecsResponse(ResharpeningSpecs specs);

    @Mapping(target = "coatingName", source = "coating.name")
    ReformingSpecsResponse toReformingSpecsResponse(ReformingSpecs specs);

    @org.mapstruct.AfterMapping
    default void linkItems(@org.mapstruct.MappingTarget Enquiry enquiry) {
        if (enquiry.getItems() != null) {
            enquiry.getItems().forEach(item -> {
                item.setEnquiry(enquiry);
                if (item.getNewToolSpecs() != null) item.getNewToolSpecs().setEnquiryItem(item);
                if (item.getResharpeningSpecs() != null) item.getResharpeningSpecs().setEnquiryItem(item);
                if (item.getReformingSpecs() != null) item.getReformingSpecs().setEnquiryItem(item);
            });
        }
    }
}
