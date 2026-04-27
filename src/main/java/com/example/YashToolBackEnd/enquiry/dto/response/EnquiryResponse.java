package com.example.YashToolBackEnd.enquiry.dto.response;

import com.example.YashToolBackEnd.enquiry.entity.Enquiry;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EnquiryResponse {
    private String customerName;
    private UUID enquiryId;
    private Boolean isUrgent;
    private String enquiryNo;
    private String status;
    private List<EnquiryItemResponse> items;
}
