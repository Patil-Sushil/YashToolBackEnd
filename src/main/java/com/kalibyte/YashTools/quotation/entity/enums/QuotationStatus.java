package com.kalibyte.YashTools.quotation.entity.enums;

import java.util.EnumSet;
import java.util.Set;

public enum QuotationStatus {

    DRAFT,
    PRICING_READY,
    PENDING_APPROVAL,
    ADMIN_APPROVED,
    ADMIN_REJECTED,
    APPROVED,
    REJECTED,
    SENT_TO_CUSTOMER,
    CUSTOMER_NEGOTIATION,
    CUSTOMER_APPROVED,
    CUSTOMER_REJECTED,
    CUSTOMER_REJECT,
    LOCKED,
    EXPIRED,
    CANCELLED;

    public static final Set<QuotationStatus> EDITABLE =
            EnumSet.of(DRAFT, PRICING_READY, ADMIN_REJECTED, REJECTED, CUSTOMER_NEGOTIATION);

    public static final Set<QuotationStatus> TERMINAL =
            EnumSet.of(CUSTOMER_APPROVED, CUSTOMER_REJECTED, CUSTOMER_REJECT, LOCKED, EXPIRED, CANCELLED);

    public static final Set<QuotationStatus> AWAITING_CUSTOMER =
            EnumSet.of(SENT_TO_CUSTOMER, CUSTOMER_NEGOTIATION);

    public boolean isEditable() { return EDITABLE.contains(this); }
    public boolean isTerminal() { return TERMINAL.contains(this); }
    public boolean canBeSentToCustomer() { return this == ADMIN_APPROVED || this == APPROVED || this == PRICING_READY; }
}