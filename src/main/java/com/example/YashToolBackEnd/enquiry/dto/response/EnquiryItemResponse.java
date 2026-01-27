package com.example.YashToolBackEnd.enquiry.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnquiryItemResponse {
    private Long itemId;
    private String orderType;
    private String toolName;
    private Boolean isTrial;
    private Integer quantity;
    private String remarks;
    private NewToolSpecsResponse newToolSpecs;
    private ResharpeningSpecsResponse resharpeningSpecs;
    private ReformingSpecsResponse reformingSpecs;
}
