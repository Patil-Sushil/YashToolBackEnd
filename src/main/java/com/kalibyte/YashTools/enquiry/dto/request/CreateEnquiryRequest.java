package com.kalibyte.YashTools.enquiry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnquiryRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    private String remarks;
    private Boolean isUrgent;
    
    @NotEmpty(message = "Enquiry must contain at least one item")
    @Valid
    private List<EnquiryItemRequest> items;
}
