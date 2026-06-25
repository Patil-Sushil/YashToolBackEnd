package com.kalibyte.YashTools.enquiry.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryResponse {

    private UUID enquiryId;
    private String enquiryNo;
    private String customerName;
    private UUID customerId;
    private Boolean isUrgent;
    private String status;
    private Boolean hasTrial;
    private Integer itemCount;
    private List<EnquiryItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String updatedBy;
    private String companyCode;
    private UUID companyId;
}