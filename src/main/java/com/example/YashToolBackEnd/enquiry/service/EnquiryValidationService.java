package com.example.YashToolBackEnd.enquiry.service;

import com.example.YashToolBackEnd.common.enums.OrderType;
import com.example.YashToolBackEnd.common.exception.BusinessException;
import com.example.YashToolBackEnd.enquiry.entity.Enquiry;
import com.example.YashToolBackEnd.enquiry.entity.EnquiryItem;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Service
public class EnquiryValidationService {

    public void validate(Enquiry enquiry) {
        // 1. Safety Check: Ensure items list is not empty
        if (CollectionUtils.isEmpty(enquiry.getItems())) {
            throw new BusinessException("Enquiry must contain at least one item.");
        }

        // 2. Check if any item is marked as a Trial
        boolean hasTrial = enquiry.getItems().stream()
                .map(EnquiryItem::getIsTrial) // Assumes field is 'isTrail' (Note: typo in your code? Should it be 'isTrial'?)
                .filter(Objects::nonNull)
                .anyMatch(Boolean::booleanValue);

        // 3. Apply Trial Rules
        if (hasTrial) {
            // Rule A: Trial enquiries can only have exactly one item
            if (enquiry.getItems().size() > 1) {
                throw new BusinessException("Trial enquiries must contain exactly one item.");
            }

            EnquiryItem item = enquiry.getItems().get(0);

            // Rule B: Trial is only for NEW_TOOL
            if (item.getOrderType() != OrderType.NEW_TOOL) {
                throw new BusinessException("Trial is allowed only for NEW_TOOL order type.");
            }

            // Rule C: Quantity must be exactly 1
            // Use long/int comparison safe for nulls if wrapper class is used
            if (item.getQuantity() == null || item.getQuantity() > 1) {
                throw new BusinessException("Trial quantity must be exactly 1.");
            }
        }

        // 4. Validate Specs for all items
        enquiry.getItems().forEach(this::validateSpecs);
    }

    private void validateSpecs(EnquiryItem item) {
        if (item.getOrderType() == null) {
            throw new BusinessException("Order Type is required.");
        }

        switch (item.getOrderType()) {
            case NEW_TOOL -> {
                if (item.getNewToolSpecs() == null)
                    throw new BusinessException("NEW_TOOL specs are missing.");
            }
            case RESHARPENING -> {
                if (item.getResharpeningSpecs() == null)
                    throw new BusinessException("RESHARPENING specs are missing.");
            }
            case REFORMING -> {
                if (item.getReformingSpecs() == null)
                    throw new BusinessException("REFORMING specs are missing.");
            }
        }
    }
}