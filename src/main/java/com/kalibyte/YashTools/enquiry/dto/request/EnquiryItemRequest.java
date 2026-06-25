package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for enquiry item creation
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>orderType is mandatory</li>
 *   <li>toolName cannot be blank</li>
 *   <li>quantity must be at least 1</li>
 *   <li>Specification object must match order type</li>
 * </ul>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryItemRequest {

    /**
     * Type of order (NEW_TOOL, RESHARPENING, REFORMING)
     */
    @NotNull(message = "Order type is required")
    private OrderType orderType;

    /**
     * Product/tool name or identifier
     */
    @NotBlank(message = "Tool name is required")
    private String toolName;

    /**
     * Quantity required
     * <p>For trial: max 1 (enforced by business logic)</p>
     * <p>For regular: min 1, no upper limit</p>
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Trial order flag (defaults to false)
     */
    @Builder.Default
    private Boolean trial = false;

    /**
     * Item-specific remarks
     */
    private String remarks;

    /**
     * Reference to technical drawing
     */
    private String drawingReference;

    // ========================================
    // Type-specific specifications
    // Only one should be provided based on orderType
    // ========================================

    /**
     * Required for NEW_TOOL orders only
     */
    @Valid
    private NewToolSpecsRequest newToolSpecs;

    /**
     * Required for REFORMING orders only
     */
    @Valid
    private ReformingSpecsRequest reformingSpecs;

    /**
     * Required for RESHARPENING orders only
     */
    @Valid
    private ResharpeningSpecsRequest resharpeningSpecs;
}