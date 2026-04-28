package com.kalibyte.YashTools.enquiry.dto.response;

import com.kalibyte.YashTools.common.enums.ResharpeningType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ResharpeningSpecsResponse {

        private ResharpeningType resharpeningType;
        private Boolean hasCoating;
        private String coatingName;

}
