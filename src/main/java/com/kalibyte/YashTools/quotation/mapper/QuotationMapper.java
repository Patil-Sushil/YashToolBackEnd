package com.kalibyte.YashTools.quotation.mapper;

import com.kalibyte.YashTools.quotation.dto.response.QuotationItemResponse;
import com.kalibyte.YashTools.quotation.dto.response.QuotationResponse;
import com.kalibyte.YashTools.quotation.entity.Quotation;
import com.kalibyte.YashTools.quotation.entity.QuotationItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    @Mapping(target = "quotationId", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "sourceEnquiryId", source = "sourceEnquiry.id")
    @Mapping(target = "parentQuotationId", source = "parentQuotation.id")
    @Mapping(target = "parentQuotationNo", source = "parentQuotation.quotationNo")
    @Mapping(target = "rootQuotationId", expression = "java(resolveRootQuotationId(quotation))")
    @Mapping(target = "rootQuotationNo", expression = "java(resolveRootQuotationNo(quotation))")
    @Mapping(target = "isRevision", expression = "java(resolveIsRevision(quotation))")
    @Mapping(target = "revisionNumber", expression = "java(resolveRevisionNumber(quotation))")
    @Mapping(target = "customerCompanyName", source = "customer.companyName")
    @Mapping(target = "customerContactPerson", source = "customer.customerName")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerMobile", source = "customer.mobileNumber")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    QuotationResponse toResponse(Quotation quotation);

    default UUID resolveRootQuotationId(Quotation quotation) {
        if (quotation == null) return null;
        Quotation curr = quotation;
        while (curr.getParentQuotation() != null) {
            curr = curr.getParentQuotation();
        }
        return curr.getId();
    }

    default String resolveRootQuotationNo(Quotation quotation) {
        if (quotation == null) return null;
        return com.kalibyte.YashTools.quotation.util.QuotationNumberGenerator.getBaseQuotationNo(quotation.getQuotationNo());
    }

    default Boolean resolveIsRevision(Quotation quotation) {
        if (quotation == null) return false;
        return quotation.getParentQuotation() != null 
                || (quotation.getQuotationNo() != null && quotation.getQuotationNo().contains("-R"));
    }

    default Integer resolveRevisionNumber(Quotation quotation) {
        if (quotation == null || quotation.getQuotationNo() == null) return 0;
        int rIdx = quotation.getQuotationNo().lastIndexOf("-R");
        if (rIdx >= 0) {
            try {
                return Integer.parseInt(quotation.getQuotationNo().substring(rIdx + 2));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    @Named("mapItems")
    default List<QuotationItemResponse> mapItems(List<QuotationItem> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Mapping(target = "itemId", source = "id")
    QuotationItemResponse toItemResponse(QuotationItem item);
}