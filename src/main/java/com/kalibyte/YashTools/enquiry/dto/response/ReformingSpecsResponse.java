package com.kalibyte.YashTools.enquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReformingSpecsResponse {

    private Boolean hasCoating;
    private String coatingName;
    private Double fluteLength;
}
