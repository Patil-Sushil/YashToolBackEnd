package com.example.YashToolBackEnd.enquiry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReformingSpecsRequest {
    // Where coating is required after reforming
    @NotNull
    private Boolean hasCoating;

    //Optional coating master ID

    private Long coatingId;

    // only flute length is required for reforming
    @NotNull
    @Positive
    private Double fluteLength;
}
