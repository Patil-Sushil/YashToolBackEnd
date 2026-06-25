package com.kalibyte.YashTools.enquiry.dto.response;

import com.kalibyte.YashTools.common.enums.OrderType;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryItemResponse {

    private UUID itemId;
    private OrderType orderType;
    private String toolName;
    private Integer quantity;
    private Boolean trial;
    private String remarks;
    private String drawingReference;
    private NewToolSpecsResponse newToolSpecs;
    private ReformingSpecsResponse reformingSpecs;
    private ResharpeningSpecsResponse resharpeningSpecs;
}