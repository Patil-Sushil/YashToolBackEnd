package com.kalibyte.YashTools.enquiry.dto.response;

import com.kalibyte.YashTools.common.enums.ResharpeningType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResharpeningSpecsResponse {

        private ResharpeningType resharpeningType;
        private Boolean hasCoating;
        private String coatingName;
        private String technicalNotes;

}