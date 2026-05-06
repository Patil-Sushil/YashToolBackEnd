package com.kalibyte.YashTools.enquiry.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
