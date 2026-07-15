package com.kalibyte.YashTools.purchase.master.paymentterms.mapper;

import com.kalibyte.YashTools.purchase.master.paymentterms.dto.request.PaymentTermsRequest;
import com.kalibyte.YashTools.purchase.master.paymentterms.dto.response.PaymentTermsResponse;
import com.kalibyte.YashTools.purchase.master.paymentterms.entity.PaymentTerms;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface PaymentTermsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    PaymentTerms toEntity(PaymentTermsRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    PaymentTermsResponse toResponse(PaymentTerms entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntityFromRequest(PaymentTermsRequest request, @MappingTarget PaymentTerms entity);
}
