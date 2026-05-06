package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryItemRequest {
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    @NotBlank(message = "Tool name is required")
    private String toolName;
    
    private Boolean isTrial;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String remarks;
    
    @Valid
    private NewToolSpecsRequest newToolSpecs;
    
    @Valid
    private ResharpeningSpecsRequest resharpeningSpecs;
    
    @Valid
    private ReformingSpecsRequest reformingSpecs;
}
