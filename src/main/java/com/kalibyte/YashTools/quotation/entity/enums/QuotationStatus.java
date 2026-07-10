package com.kalibyte.YashTools.quotation.entity.enums;

import java.util.EnumSet;
import java.util.Set;

public enum QuotationStatus {

    DRAFT,
    PRICING_READY,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SENT_TO_CUSTOMER,
    CUSTOMER_NEGOTIATION,
    CUSTOMER_APPROVED,
    CUSTOMER_REJECTED,
    LOCKED,
    EXPIRED,
    CANCELLED;

    public static final Set<QuotationStatus> EDITABLE =
            EnumSet.of(DRAFT, PRICING_READY, REJECTED, CUSTOMER_NEGOTIATION);

    public static final Set<QuotationStatus> TERMINAL =
            EnumSet.of(CUSTOMER_APPROVED, CUSTOMER_REJECTED, LOCKED, EXPIRED, CANCELLED);

    public static final Set<QuotationStatus> AWAITING_CUSTOMER =
            EnumSet.of(SENT_TO_CUSTOMER, CUSTOMER_NEGOTIATION);

    public boolean isEditable() { return EDITABLE.contains(this); }
    public boolean isTerminal() { return TERMINAL.contains(this); }
    public boolean canBeSentToCustomer() { return this == APPROVED || this == PRICING_READY; }
}