package com.kalibyte.YashTools.enquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewToolSpecsResponse {
    private boolean hasCoating;
    private String coatingName;
    private String rawMaterialName;

    private Double diameter;
    private Double fluteLength;
    private Double shankDiameter;
    private Double overallLength;
}
