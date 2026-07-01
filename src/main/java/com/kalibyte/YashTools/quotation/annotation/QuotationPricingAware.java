package com.kalibyte.YashTools.quotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for entities that participate in the quotation pricing pipeline.
 * <p>Use on a DTO field to indicate that the QuotationAuditAspect should
 * snapshoot its value into the audit log when the annotation is hit.</p>
 *
 * <pre>
 *   &#64;QuotationPricingAware(valueSource = QuotationPricingAware.ValueSource.CURRENT_USER)
 *   private BigDecimal discountPercentage;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QuotationPricingAware {

    /** Where the audit-aspect pulls the value from at runtime. */
    ValueSource valueSource() default ValueSource.REQUEST_BODY;

    enum ValueSource {
        REQUEST_BODY,
        SECURITY_CONTEXT,
        COMPANY_CONTEXT
    }
}