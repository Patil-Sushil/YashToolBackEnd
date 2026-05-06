package com.kalibyte.YashTools.enquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponse {
    private String customerName;
    private UUID enquiryId;
    private Boolean isUrgent;
    private String enquiryNo;
    private String status;
    private List<EnquiryItemResponse> items;
}
