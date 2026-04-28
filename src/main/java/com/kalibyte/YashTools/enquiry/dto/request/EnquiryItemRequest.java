package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.OrderType;
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
