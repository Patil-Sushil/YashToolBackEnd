package com.kalibyte.YashTools.enquiry.entity.enums;

/**
 * Lifecycle states of an enquiry
 */
public enum EnquiryStatus {
    /**
     * Initial state after enquiry creation
     */
    CREATED,

    /**
     * Under technical review
     */
    UNDER_REVIEW,

    /**
     * Quotation prepared and sent to customer
     */
    QUOTED,

    /**
     * Customer accepted the quotation
     */
    ACCEPTED,

    /**
     * Customer rejected or enquiry expired
     */
    CLOSED
}