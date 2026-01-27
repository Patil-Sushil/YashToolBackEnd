package com.example.YashToolBackEnd.enquiry.dto.request;

import com.example.YashToolBackEnd.enquiry.entity.Enquiry;
import lombok.Data;

import java.util.List;

@Data
public class CreateEnquiryRequest {
    private Long customerId;
    private String remarks;
    private Boolean isUrgent;
    private List<EnquiryItemRequest> items;
}
