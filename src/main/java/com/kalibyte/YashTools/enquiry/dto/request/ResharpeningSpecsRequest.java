package com.kalibyte.YashTools.enquiry.dto.request;

import com.kalibyte.YashTools.common.enums.ResharpeningType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResharpeningSpecsRequest {
    // Applicable only for End mill
    private ResharpeningType resharpeningType;

    // Where the coating is required after resharpening
    @NotNull
    private Boolean hasCoating;

    // Optional coating master ID
    private Long coatingId;
}
