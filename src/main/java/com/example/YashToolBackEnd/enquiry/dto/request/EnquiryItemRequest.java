package com.example.YashToolBackEnd.enquiry.dto.request;

import com.example.YashToolBackEnd.common.enums.OrderType;
import com.example.YashToolBackEnd.enquiry.entity.NewToolSpecs;
import lombok.Data;

@Data
public class EnquiryItemRequest {
    private OrderType orderType;
    private String toolName;
    private Boolean isTrial;
    private Integer quantity;
    private String remarks;
    private NewToolSpecsRequest newToolSpecs;
    private ResharpeningSpecsRequest resharpeningSpecs;
    private ReformingSpecsRequest reformingSpecs;

}
