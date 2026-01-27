package com.example.YashToolBackEnd.enquiry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NewToolSpecsRequest {
    // Where the coating is required (e.g., Full, Tip, None)
    @NotNull
    private Boolean hasCoating;

    // Coating Master ID (required only if hasCoating = true)
    private Long coatingId;

    // Raw Material master ID (required)
    @NotNull(message = "Raw material is mandotory for new tool")
    private Long rawMaterialId;

    @NotNull
    @Positive
    private Double diameter;

    @NotNull
    @Positive
    private Double fluteLength;

    @NotNull
    @Positive
    private Double shankDiameter;

    @NotNull
    @Positive
    private Double overallLength;

}
