package com.example.YashToolBackEnd.enquiry.dto.request;

import com.example.YashToolBackEnd.common.enums.ResharpeningType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResharpeningSpecsRequest {
    // Applicable only for End mill
    private ResharpeningType resharpeningType;

    // Where the coating is required after resharpening
    @NotNull
    private Boolean hasCoating;

    // Optional coating master ID
    private Long coatingId;
}
