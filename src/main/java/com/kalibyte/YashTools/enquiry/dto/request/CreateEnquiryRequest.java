package com.kalibyte.YashTools.enquiry.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateEnquiryRequest {
    private UUID customerId;
    private String remarks;
    private Boolean isUrgent;
    private List<EnquiryItemRequest> items;
}
