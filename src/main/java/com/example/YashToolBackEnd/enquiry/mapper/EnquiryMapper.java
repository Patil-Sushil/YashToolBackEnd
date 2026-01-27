package com.example.YashToolBackEnd.enquiry.mapper;

import com.example.YashToolBackEnd.customer.entity.Customer;
import com.example.YashToolBackEnd.enquiry.dto.request.*;
import com.example.YashToolBackEnd.enquiry.dto.response.*;
import com.example.YashToolBackEnd.enquiry.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class EnquiryMapper {

    /* =====================================================
       REQUEST → ENTITY
       ===================================================== */

    public static Enquiry toEntity(CreateEnquiryRequest request, Customer customer) {

        Enquiry enquiry = new Enquiry();
        enquiry.setCustomer(customer);
        enquiry.setRemarks(request.getRemarks());

        // ✅ NEW: Urgent flag (default = false)
        enquiry.setIsUrgent(Boolean.TRUE.equals(request.getIsUrgent()));

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            enquiry.setItems(
                    request.getItems()
                            .stream()
                            .map(itemDto -> toItemEntity(itemDto, enquiry))
                            .collect(Collectors.toList())
            );
        }

        return enquiry;
    }

    private static EnquiryItem toItemEntity(
            EnquiryItemRequest dto,
            Enquiry enquiry) {

        EnquiryItem item = new EnquiryItem();

        // -------- Common fields --------
        item.setEnquiry(enquiry);           // 🔑 JPA parent reference
        item.setOrderType(dto.getOrderType());
        item.setToolName(dto.getToolName());
        item.setIsTrial(dto.getIsTrial());
        item.setQuantity(dto.getQuantity());
        item.setRemarks(dto.getRemarks());

        // -------- Order-type specific specs --------
        switch (dto.getOrderType()) {

            case NEW_TOOL -> {
                NewToolSpecs specs =
                        toNewToolSpecs(dto.getNewToolSpecs(), item);
                item.setNewToolSpecs(specs);
            }

            case RESHARPENING -> {
                ResharpeningSpecs specs =
                        toResharpeningSpecs(dto.getResharpeningSpecs(), item);
                item.setResharpeningSpecs(specs);
            }

            case REFORMING -> {
                ReformingSpecs specs =
                        toReformingSpecs(dto.getReformingSpecs(), item);
                item.setReformingSpecs(specs);
            }
        }

        return item;
    }

    private static NewToolSpecs toNewToolSpecs(
            NewToolSpecsRequest dto,
            EnquiryItem item) {

        if (dto == null) return null;

        NewToolSpecs specs = new NewToolSpecs();
        specs.setEnquiryItem(item);          // 🔑 FK link
        specs.setHasCoating(dto.getHasCoating());
        specs.setDiameter(dto.getDiameter());
        specs.setFluteLength(dto.getFluteLength());
        specs.setShankDiameter(dto.getShankDiameter());
        specs.setOverallLength(dto.getOverallLength());

        // NOTE:
        // coating & rawMaterial are injected in Service layer

        return specs;
    }

    private static ResharpeningSpecs toResharpeningSpecs(
            ResharpeningSpecsRequest dto,
            EnquiryItem item) {

        if (dto == null) return null;

        ResharpeningSpecs specs = new ResharpeningSpecs();
        specs.setEnquiryItem(item);
        specs.setResharpeningType(dto.getResharpeningType());
        specs.setHasCoating(dto.getHasCoating());

        return specs;
    }

    private static ReformingSpecs toReformingSpecs(
            ReformingSpecsRequest dto,
            EnquiryItem item) {

        if (dto == null) return null;

        ReformingSpecs specs = new ReformingSpecs();
        specs.setEnquiryItem(item);
        specs.setHasCoating(dto.getHasCoating());
        specs.setFluteLength(dto.getFluteLength());

        return specs;
    }

    /* =====================================================
       ENTITY → RESPONSE
       ===================================================== */

    public static EnquiryResponse toResponse(Enquiry enquiry) {

        return EnquiryResponse.builder()
                .enquiryId(enquiry.getId())
                .enquiryNo(enquiry.getEnquiryNo())
                .status(enquiry.getStatus().name())
                .isUrgent(enquiry.getIsUrgent())      // ✅ NEW
                .customerName(
                        enquiry.getCustomer() != null
                                ? enquiry.getCustomer().getCompanyName()
                                : null
                )
                .items(toItemResponseList(enquiry.getItems()))
                .build();
    }

    private static List<EnquiryItemResponse> toItemResponseList(
            List<EnquiryItem> items) {

        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> EnquiryItemResponse.builder()
                        .itemId(item.getId())
                        .orderType(item.getOrderType().name())
                        .toolName(item.getToolName())
                        .isTrial(item.getIsTrial())
                        .quantity(item.getQuantity())
                        .remarks(item.getRemarks())
                        .newToolSpecs(
                                toNewToolSpecsResponse(item.getNewToolSpecs()))
                        .resharpeningSpecs(
                                toResharpeningSpecsResponse(
                                        item.getResharpeningSpecs()))
                        .reformingSpecs(
                                toReformingSpecsResponse(
                                        item.getReformingSpecs()))
                        .build())
                .collect(Collectors.toList());
    }

    private static NewToolSpecsResponse toNewToolSpecsResponse(
            NewToolSpecs specs) {

        if (specs == null) return null;

        return NewToolSpecsResponse.builder()
                .hasCoating(specs.getHasCoating())
                .coatingName(
                        specs.getCoating() != null
                                ? specs.getCoating().getName()
                                : null
                )
                .rawMaterialName(
                        specs.getRawMaterial() != null
                                ? specs.getRawMaterial().getName()
                                : null
                )
                .diameter(specs.getDiameter())
                .fluteLength(specs.getFluteLength())
                .shankDiameter(specs.getShankDiameter())
                .overallLength(specs.getOverallLength())
                .build();
    }

    private static ResharpeningSpecsResponse toResharpeningSpecsResponse(
            ResharpeningSpecs specs) {

        if (specs == null) return null;

        return ResharpeningSpecsResponse.builder()
                .resharpeningType(specs.getResharpeningType())
                .hasCoating(specs.getHasCoating())
                .coatingName(
                        specs.getCoating() != null
                                ? specs.getCoating().getName()
                                : null
                )
                .build();
    }

    private static ReformingSpecsResponse toReformingSpecsResponse(
            ReformingSpecs specs) {

        if (specs == null) return null;

        return ReformingSpecsResponse.builder()
                .hasCoating(specs.getHasCoating())
                .coatingName(
                        specs.getCoating() != null
                                ? specs.getCoating().getName()
                                : null
                )
                .fluteLength(specs.getFluteLength())
                .build();
    }
}
