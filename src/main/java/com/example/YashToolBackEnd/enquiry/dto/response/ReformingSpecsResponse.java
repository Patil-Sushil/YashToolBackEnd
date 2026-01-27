package com.example.YashToolBackEnd.enquiry.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReformingSpecsResponse {

    private Boolean hasCoating;
    private String coatingName;
    private Double fluteLength;
}
