package com.kalibyte.YashTools.enquiry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
