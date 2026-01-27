package com.example.YashToolBackEnd.enquiry.dto.response;

import com.example.YashToolBackEnd.common.enums.ResharpeningType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ResharpeningSpecsResponse {

        private ResharpeningType resharpeningType;
        private Boolean hasCoating;
        private String coatingName;

}
