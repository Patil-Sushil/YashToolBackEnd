package com.kalibyte.YashTools.enquiry.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.enquiry.entity.Enquiry;
import com.kalibyte.YashTools.enquiry.entity.EnquiryItem;
import com.kalibyte.YashTools.enquiry.entity.NewToolSpecs;
import com.kalibyte.YashTools.enquiry.entity.ReformingSpecs;
import com.kalibyte.YashTools.enquiry.entity.ResharpeningSpecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Service for validating enquiry business rules
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Enquiry must contain at least one item</li>
 *   <li>Trial items: max quantity = 1 (for all order types)</li>
 *   <li>Each item must have appropriate specifications for its order type</li>
 *   <li>Coating type must be provided if coatingRequired = true</li>
 * </ul>
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Slf4j
@Service
public class EnquiryValidationService {

    /**
     * Maximum allowed quantity for trial orders
     */
    private static final int MAX_TRIAL_QUANTITY = 1;

    /**
     * Validate enquiry against all business rules
     *
     * @param enquiry the enquiry to validate
     * @throws BusinessException if validation fails
     */
    public void validate(Enquiry enquiry) {
        log.debug("Validating enquiry for customer: {}", enquiry.getCustomer().getId());

        validateItemsExist(enquiry);
        validateTrialRules(enquiry);
        validateItemSpecifications(enquiry);

        log.debug("Enquiry validation successful");
    }

    /**
     * Ensure enquiry contains at least one item
     */
    private void validateItemsExist(Enquiry enquiry) {
        if (CollectionUtils.isEmpty(enquiry.getItems())) {
            throw new BusinessException("Enquiry must contain at least one item");
        }
    }

    /**
     * Validate trial order business rules
     * <p>Trial orders allowed for all order types (NEW_TOOL, RESHARPENING, REFORMING)</p>
     * <p>Maximum quantity for trial: 1</p>
     * <p>User can manually set quantity, but it will be validated</p>
     */
    private void validateTrialRules(Enquiry enquiry) {
        enquiry.getItems().stream()
                .filter(EnquiryItem::isTrial)
                .forEach(this::validateTrialItem);
    }

    /**
     * Validate individual trial item
     */
    private void validateTrialItem(EnquiryItem item) {
        if (item.getQuantity() > MAX_TRIAL_QUANTITY) {
            throw new BusinessException(
                    String.format(
                            "Trial quantity cannot exceed %d for %s order. Current quantity: %d",
                            MAX_TRIAL_QUANTITY,
                            item.getOrderType(),
                            item.getQuantity()
                    )
            );
        }

        log.debug("Trial item validated: orderType={}, quantity={}",
                item.getOrderType(), item.getQuantity());
    }

    /**
     * Validate that each item has appropriate specifications based on order type
     */
    private void validateItemSpecifications(Enquiry enquiry) {
        enquiry.getItems().forEach(this::validateItemSpecs);
    }

    /**
     * Validate specifications for a single item
     */
    private void validateItemSpecs(EnquiryItem item) {
        if (item.getOrderType() == null) {
            throw new BusinessException("Order type is required for all enquiry items");
        }

        switch (item.getOrderType()) {
            case NEW_TOOL -> validateNewToolSpecs(item);
            case REFORMING -> validateReformingSpecs(item);
            case RESHARPENING -> validateResharpeningSpecs(item);
            default -> throw new BusinessException(
                    "Invalid order type: " + item.getOrderType()
            );
        }
    }

    /**
     * Validate NEW_TOOL specifications
     */
    private void validateNewToolSpecs(EnquiryItem item) {
        if (item.getNewToolSpecs() == null) {
            throw new BusinessException(
                    "NEW_TOOL specifications are required for new tool orders"
            );
        }

        // Ensure other spec types are null
        if (item.getReformingSpecs() != null || item.getResharpeningSpecs() != null) {
            throw new BusinessException(
                    "NEW_TOOL orders cannot have reforming or resharpening specifications"
            );
        }

        // Validate coating logic
        validateCoatingLogic(item.getNewToolSpecs());
    }

    /**
     * Validate REFORMING specifications
     */
    private void validateReformingSpecs(EnquiryItem item) {
        if (item.getReformingSpecs() == null) {
            throw new BusinessException(
                    "REFORMING specifications are required for reforming orders"
            );
        }

        // Ensure other spec types are null
        if (item.getNewToolSpecs() != null || item.getResharpeningSpecs() != null) {
            throw new BusinessException(
                    "REFORMING orders cannot have new tool or resharpening specifications"
            );
        }

        // Validate coating logic
        validateCoatingLogic(item.getReformingSpecs());
    }

    /**
     * Validate RESHARPENING specifications
     */
    private void validateResharpeningSpecs(EnquiryItem item) {
        if (item.getResharpeningSpecs() == null) {
            throw new BusinessException(
                    "RESHARPENING specifications are required for resharpening orders"
            );
        }

        // Ensure other spec types are null
        if (item.getNewToolSpecs() != null || item.getReformingSpecs() != null) {
            throw new BusinessException(
                    "RESHARPENING orders cannot have new tool or reforming specifications"
            );
        }

        // Validate coating logic
        validateCoatingLogic(item.getResharpeningSpecs());
    }

    /**
     * Validate coating logic for NewToolSpecs
     * <p>If coatingRequired = true, coatingType must be specified</p>
     * <p>If coatingRequired = false, coatingType should be null</p>
     */
    private void validateCoatingLogic(NewToolSpecs specs) {
        if (Boolean.TRUE.equals(specs.getCoatingRequired())) {
            if (specs.getCoatingType() == null) {
                throw new BusinessException(
                        "Coating type must be specified when coating is required"
                );
            }
        } else {
            if (specs.getCoatingType() != null) {
                throw new BusinessException(
                        "Coating type should not be specified when coating is not required"
                );
            }
        }
    }

    /**
     * Validate coating logic for ReformingSpecs
     */
    private void validateCoatingLogic(ReformingSpecs specs) {
        if (Boolean.TRUE.equals(specs.getCoatingRequired())) {
            if (specs.getCoatingType() == null) {
                throw new BusinessException(
                        "Coating type must be specified when coating is required"
                );
            }
        } else {
            if (specs.getCoatingType() != null) {
                throw new BusinessException(
                        "Coating type should not be specified when coating is not required"
                );
            }
        }
    }

    /**
     * Validate coating logic for ResharpeningSpecs
     */
    private void validateCoatingLogic(ResharpeningSpecs specs) {
        if (Boolean.TRUE.equals(specs.getCoatingRequired())) {
            if (specs.getCoatingType() == null) {
                throw new BusinessException(
                        "Coating type must be specified when coating is required"
                );
            }
        } else {
            if (specs.getCoatingType() != null) {
                throw new BusinessException(
                        "Coating type should not be specified when coating is not required"
                );
            }
        }
    }
}