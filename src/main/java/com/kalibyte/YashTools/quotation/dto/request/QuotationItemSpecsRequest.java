package com.kalibyte.YashTools.quotation.dto.request;

import com.kalibyte.YashTools.common.enums.CoatingType;
import com.kalibyte.YashTools.common.enums.ResharpeningType;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialGrade;
import com.kalibyte.YashTools.enquiry.entity.enums.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationItemSpecsRequest {

    private MaterialType materialType;
    private MaterialGrade materialGrade;

    @Builder.Default
    private Boolean coatingRequired = false;
    private CoatingType coatingType;

    private ResharpeningType resharpeningType;

    private Double diameter;
    private Double fluteLength;
    private Double shankDiameter;

    private String technicalNotes;

    private String damageLevel; // Minor / Medium / Major
    private Boolean specialGeometry;
    private Boolean specialProfile;
    private Boolean expressDelivery;
}