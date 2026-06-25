package com.kalibyte.YashTools.enquiry.dto.response;

import lombok.*;

/**
 * Response DTO for reforming specifications
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReformingSpecsResponse {

    /**
     * Coating required flag
     */
    private Boolean coatingRequired;

    /**
     * Coating type name (null if not required)
     */
    private String coatingType;

    /**
     * Target flute length (mm)
     */
    private Double fluteLength;

    /**
     * Technical notes
     */
    private String technicalNotes;
}