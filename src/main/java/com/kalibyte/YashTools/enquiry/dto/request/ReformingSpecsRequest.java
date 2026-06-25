package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.CoatingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO for reforming specifications
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReformingSpecsRequest {

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
     * Target flute length in mm
     */
    @NotNull(message = "Flute length is required")
    @Positive(message = "Flute length must be positive")
    private Double fluteLength;

    /**
     * Additional technical notes
     */
    private String technicalNotes;
}