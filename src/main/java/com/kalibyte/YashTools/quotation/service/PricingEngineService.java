package com.kalibyte.YashTools.quotation.service;

import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.dto.response.PricingBreakdown;

import java.util.UUID;

public interface PricingEngineService {
    PricingBreakdown priceItem(QuotationItemRequest request);
    void recomputeQuotation(UUID quotationId);
}