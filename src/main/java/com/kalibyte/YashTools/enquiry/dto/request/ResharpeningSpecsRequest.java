package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.CoatingType;
import com.kalibyte.YashTools.common.enums.ResharpeningType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for resharpening specifications
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResharpeningSpecsRequest {

    /**
     * Level of resharpening service
     */
    @NotNull(message = "Resharpening type is required")
    private ResharpeningType resharpeningType;

    /**
     * Coating requirement flag
     */
    @NotNull(message = "Coating requirement must be specified")
    @Builder.Default
    private Boolean coatingRequired = false;

    /**
     * Specific coating type (required if coatingRequired = true)
     */
    private CoatingType coatingType;

    /**
     * Additional technical notes
     */
    private String technicalNotes;
}