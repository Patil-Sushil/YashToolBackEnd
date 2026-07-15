package com.kalibyte.YashTools.purchase.master.vendor.mapper;

import com.kalibyte.YashTools.purchase.master.vendor.dto.request.VendorRequest;
import com.kalibyte.YashTools.purchase.master.vendor.dto.response.VendorResponse;
import com.kalibyte.YashTools.purchase.master.vendor.entity.Vendor;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        builder = @Builder(disableBuilder = true)
)
public interface VendorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "paymentTerms", ignore = true)
    Vendor toEntity(VendorRequest request);

    @Mapping(target = "paymentTermsId", source = "paymentTerms.id")
    @Mapping(target = "paymentTermsCode", source = "paymentTerms.code")
    @Mapping(target = "paymentTermsName", source = "paymentTerms.name")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyCode", source = "company.code")
    @Mapping(target = "companyName", source = "company.name")
    VendorResponse toResponse(Vendor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "paymentTerms", ignore = true)
    void updateEntityFromRequest(VendorRequest request, @MappingTarget Vendor entity);
}
