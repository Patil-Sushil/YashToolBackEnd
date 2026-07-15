package com.kalibyte.YashTools.purchase.master.vendor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    private UUID id;
    private String vendorName;
    private String gstin;
    private String pan;
    private String contactDetails;
    private String address;
    private UUID paymentTermsId;
    private String paymentTermsCode;
    private String paymentTermsName;
    private Boolean active;
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
