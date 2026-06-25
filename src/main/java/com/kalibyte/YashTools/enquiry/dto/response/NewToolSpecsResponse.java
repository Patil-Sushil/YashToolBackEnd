package com.kalibyte.YashTools.enquiry.dto.response;

import lombok.*;

/**
 * Response DTO for new tool specifications
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewToolSpecsResponse {

    /**
     * Material type code
     */
    private String materialType;

    /**
     * Material type display name
     */
    private String materialTypeDisplay;

    /**
     * Material grade code
     */
    private String materialGrade;

    /**
     * Material grade description
     */
    private String materialGradeDescription;

    /**
     * Coating required flag
     */
    private Boolean coatingRequired;

    /**
     * Coating type name (null if not required)
     */
    private String coatingType;

    /**
     * Tool diameter (mm)
     */
    private Double diameter;

    /**
     * Flute length (mm)
     */
    private Double fluteLength;

    /**
     * Shank diameter (mm)
     */
    private Double shankDiameter;

    /**
     * Overall length (mm)
     */
    private Double overallLength;

    /**
     * Technical notes
     */
    private String technicalNotes;
}