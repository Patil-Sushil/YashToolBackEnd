package com.kalibyte.YashTools.purchase.master.vendor.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequest {

    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    private String gstin;
    private String pan;
    private String contactDetails;
    private String address;
    private UUID paymentTermsId;
    private Boolean active;
}
