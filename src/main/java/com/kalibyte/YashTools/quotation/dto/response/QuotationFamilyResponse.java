package com.kalibyte.YashTools.quotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationFamilyResponse {

    private UUID rootQuotationId;
    private String rootQuotationNo;
    private QuotationResponse rootQuotation;
    private List<QuotationResponse> revisions;
    private int totalRevisions;
    private QuotationResponse latestRevision;
}
