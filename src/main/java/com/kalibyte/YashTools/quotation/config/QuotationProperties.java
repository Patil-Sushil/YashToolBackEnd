package com.kalibyte.YashTools.quotation.config;

import com.kalibyte.YashTools.quotation.constants.QuotationConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized quotation configuration bound from {@code app.quotation.*}.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.quotation")
public class QuotationProperties {

    private int defaultValidityDays = QuotationConstants.DEFAULT_VALIDITY_DAYS;
    private double approvalThresholdPercentage = QuotationConstants.DISCOUNT_APPROVAL_THRESHOLD_PERCENT;
    private double defaultCgstPercentage = QuotationConstants.DEFAULT_CGST_PERCENTAGE;
    private double defaultSgstPercentage = QuotationConstants.DEFAULT_SGST_PERCENTAGE;
    private double defaultIgstPercentage = QuotationConstants.DEFAULT_IGST_PERCENTAGE;
    private int maxRevisions = QuotationConstants.MAX_REVISIONS_PER_QUOTATION;
    private boolean autoApplyCoatingCharge = true;
    private java.math.BigDecimal defaultCoatingCharge = new java.math.BigDecimal("50.00");
}