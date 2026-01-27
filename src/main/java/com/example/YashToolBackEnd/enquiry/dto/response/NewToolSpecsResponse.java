package com.example.YashToolBackEnd.enquiry.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewToolSpecsResponse {
    private boolean hasCoating;
    private String coatingName;
    private String rawMaterialName;

    private Double diameter;
    private Double fluteLength;
    private Double shankDiameter;
    private Double overallLength;
}
