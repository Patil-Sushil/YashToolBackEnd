package com.kalibyte.YashTools.quotation.validator;

import com.kalibyte.YashTools.quotation.dto.request.QuotationItemRequest;
import com.kalibyte.YashTools.quotation.exception.PricingException;
import com.kalibyte.YashTools.quotation.exception.QuotationStateException;
import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Centralized business-rule checks for quotation payloads.
 */
@Component
public class QuotationValidator {

    public void validateItems(List<QuotationItemRequest> items) {
        if (items == null || items.isEmpty())
            throw new QuotationStateException("Quotation must contain at least one item");
        for (int i = 0; i < items.size(); i++) {
            validateItem(items.get(i), i + 1);
        }
    }

    public void validateItem(QuotationItemRequest item, int lineNo) {
        if (item.getOrderType() == null)
            throw new PricingException("Line " + lineNo + ": orderType required");
        if (item.getToolName() == null || item.getToolName().isBlank())
            throw new PricingException("Line " + lineNo + ": toolName required");
        if (item.getItemName() == null || item.getItemName().isBlank())
            throw new PricingException("Line " + lineNo + ": itemName required");
        if (item.getOverallLength() == null || item.getOverallLength() <= 0)
            throw new PricingException("Line " + lineNo + ": overallLength must be > 0");
        if (item.getQuantity() == null || item.getQuantity() <= 0)
            throw new PricingException("Line " + lineNo + ": quantity must be > 0");
        
        if (Boolean.TRUE.equals(item.getTrial())) {
            if (item.getOrderType() != com.kalibyte.YashTools.common.enums.OrderType.NEW_TOOL) {
                throw new PricingException("Line " + lineNo + ": Trial feature is only applicable for NEW_TOOL orders");
            }
            if (item.getQuantity() != 1) {
                throw new PricingException("Line " + lineNo + ": Trial quantity must be exactly 1");
            }
        }

        if (item.getUserMultiplier() == null
                || item.getUserMultiplier().compareTo(BigDecimal.valueOf(QuotationConstants.MIN_MULTIPLIER)) < 0
                || item.getUserMultiplier().compareTo(BigDecimal.valueOf(QuotationConstants.MAX_MULTIPLIER)) > 0)
            throw new PricingException("Line " + lineNo + ": multiplier must be in ["
                    + QuotationConstants.MIN_MULTIPLIER + ", " + QuotationConstants.MAX_MULTIPLIER + "]");
    }
}