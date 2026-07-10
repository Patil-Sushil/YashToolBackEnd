package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.CoatingType;

import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO for new tool specifications
 *
 * <p><b>Coating Validation:</b></p>
 * <ul>
 *   <li>If coatingRequired = true → coatingType must be provided</li>
 *   <li>If coatingRequired = false → coatingType should be null</li>
 * </ul>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewToolSpecsRequest {

    /**
     * Base material type
     */
    @NotNull(message = "Material type is required")
    private MaterialType materialType;


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
     * Tool diameter in mm
     */
    @NotNull(message = "Diameter is required")
    @Positive(message = "Diameter must be positive")
    private Double diameter;

    /**
     * Flute length in mm
     */
    @NotNull(message = "Flute length is required")
    @Positive(message = "Flute length must be positive")
    private Double fluteLength;

    /**
     * Shank diameter in mm
     */
    @NotNull(message = "Shank diameter is required")
    @Positive(message = "Shank diameter must be positive")
    private Double shankDiameter;

    /**
     * Overall length in mm
     */
    @NotNull(message = "Overall length is required")
    @Positive(message = "Overall length must be positive")
    private Double overallLength;

    /**
     * Additional technical notes
     */
    private String technicalNotes;
}