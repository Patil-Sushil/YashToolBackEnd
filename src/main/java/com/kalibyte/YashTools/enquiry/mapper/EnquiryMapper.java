package com.kalibyte.YashTools.enquiry.mapper;

import com.kalibyte.YashTools.customer.entity.Customer;
import com.kalibyte.YashTools.enquiry.dto.request.*;
import com.kalibyte.YashTools.enquiry.dto.response.*;
import com.kalibyte.YashTools.enquiry.entity.*;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface EnquiryMapper {

    // ========================================
    // REQUEST TO ENTITY MAPPINGS
    // ========================================

    default Enquiry toEntity(CreateEnquiryRequest request, Customer customer) {
        Enquiry enquiry = Enquiry.builder()
                .customer(customer)
                .isUrgent(request.getIsUrgent() != null ? request.getIsUrgent() : false)
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            request.getItems().forEach(itemRequest -> {
                EnquiryItem item = toItemEntity(itemRequest);
                enquiry.addItem(item);
                linkSpecifications(item, itemRequest);
            });
        }

        return enquiry;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiry", ignore = true)
    @Mapping(target = "newToolSpecs", ignore = true)
    @Mapping(target = "reformingSpecs", ignore = true)
    @Mapping(target = "resharpeningSpecs", ignore = true)
    EnquiryItem toItemEntity(EnquiryItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    NewToolSpecs toNewToolSpecsEntity(NewToolSpecsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    ReformingSpecs toReformingSpecsEntity(ReformingSpecsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enquiryItem", ignore = true)
    ResharpeningSpecs toResharpeningSpecsEntity(ResharpeningSpecsRequest request);

    // ========================================
    // ENTITY TO RESPONSE MAPPINGS
    // ========================================

    @Mapping(target = "enquiryId", source = "id")
    @Mapping(target = "customerName", source = "customer.companyName")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "hasTrial", expression = "java(enquiry.hasTrial())")
    @Mapping(target = "itemCount", expression = "java(enquiry.getItemCount())")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyId", source = "company.id")
    EnquiryResponse toResponse(Enquiry enquiry);

    @Mapping(target = "itemId", source = "id")
    EnquiryItemResponse toItemResponse(EnquiryItem item);

    @Mapping(target = "materialType", expression = "java(specs.getMaterialType() != null ? specs.getMaterialType().name() : null)")
    @Mapping(target = "materialTypeDisplay", expression = "java(specs.getMaterialType() != null ? specs.getMaterialType().getDisplayName() : null)")
    @Mapping(target = "coatingRequired", source = "coatingRequired")
    @Mapping(target = "coatingType", expression = "java(specs.getCoatingType() != null ? specs.getCoatingType().name() : null)")
    @Mapping(target = "diameter", source = "diameter")
    @Mapping(target = "fluteLength", source = "fluteLength")
    @Mapping(target = "shankDiameter", source = "shankDiameter")
    @Mapping(target = "overallLength", source = "overallLength")
    @Mapping(target = "technicalNotes", source = "technicalNotes")
    NewToolSpecsResponse toNewToolSpecsResponse(NewToolSpecs specs);

    @Mapping(target = "coatingRequired", source = "coatingRequired")
    @Mapping(target = "coatingType", expression = "java(specs.getCoatingType() != null ? specs.getCoatingType().name() : null)")
    @Mapping(target = "fluteLength", source = "fluteLength")
    @Mapping(target = "technicalNotes", source = "technicalNotes")
    ReformingSpecsResponse toReformingSpecsResponse(ReformingSpecs specs);

    @Mapping(target = "resharpeningType", source = "resharpeningType")
    @Mapping(target = "hasCoating", source = "coatingRequired")
    @Mapping(target = "coatingName", expression = "java(specs.getCoatingType() != null ? specs.getCoatingType().name() : null)")  // ✅ Changed from coatingType to coatingName
    @Mapping(target = "technicalNotes", source = "technicalNotes")
    ResharpeningSpecsResponse toResharpeningSpecsResponse(ResharpeningSpecs specs);

    // ========================================
    // HELPER METHODS
    // ========================================

    default void linkSpecifications(EnquiryItem item, EnquiryItemRequest request) {
        switch (item.getOrderType()) {
            case NEW_TOOL -> {
                if (request.getNewToolSpecs() != null) {
                    NewToolSpecs specs = toNewToolSpecsEntity(request.getNewToolSpecs());
                    item.setNewToolSpecs(specs);
                }
            }
            case REFORMING -> {
                if (request.getReformingSpecs() != null) {
                    ReformingSpecs specs = toReformingSpecsEntity(request.getReformingSpecs());
                    item.setReformingSpecs(specs);
                }
            }
            case RESHARPENING -> {
                if (request.getResharpeningSpecs() != null) {
                    ResharpeningSpecs specs = toResharpeningSpecsEntity(request.getResharpeningSpecs());
                    item.setResharpeningSpecs(specs);
                }
            }
        }
    }
}