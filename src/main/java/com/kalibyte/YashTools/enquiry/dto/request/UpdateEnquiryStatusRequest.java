package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.enquiry.entity.enums.EnquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating enquiry status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnquiryStatusRequest {

    /**
     * Target status for the enquiry
     */
    @NotNull(message = "Status is required")
    private EnquiryStatus status;

    /**
     * Optional remarks explaining the status change
     */
    private String remarks;
}