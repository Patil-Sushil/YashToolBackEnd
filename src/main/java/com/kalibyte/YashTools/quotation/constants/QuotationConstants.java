package com.kalibyte.YashTools.quotation.constants;

public final class QuotationConstants {

    private QuotationConstants() {}

    // Number format
    public static final String QUOTATION_NUMBER_PREFIX = "QT";
    public static final int QUOTATION_NUMBER_SEQUENCE_LENGTH = 6;

    // Pricing
    public static final double STANDARD_ROD_LENGTH_MM = 330.0;
    public static final double DEFAULT_USER_MULTIPLIER = 1.0;
    public static final double MIN_MULTIPLIER = 0.1;
    public static final double MAX_MULTIPLIER = 10.0;

    // Tax
    public static final double DEFAULT_CGST_PERCENTAGE = 9.0;
    public static final double DEFAULT_SGST_PERCENTAGE = 9.0;
    public static final double DEFAULT_IGST_PERCENTAGE = 18.0;

    // Validity
    public static final int DEFAULT_VALIDITY_DAYS = 30;

    // Approval
    public static final double DISCOUNT_APPROVAL_THRESHOLD_PERCENT = 20.0;

    // Rate chart
    public static final String RATE_CHART_TABLE_HYPERION_ROD = "hyperion_rod_net_price";

    // Revisions
    public static final int MAX_REVISIONS_PER_QUOTATION = 20;

    // Source type
    public static final String SOURCE_TYPE_FROM_ENQUIRY = "FROM_ENQUIRY";
    public static final String SOURCE_TYPE_DIRECT = "DIRECT";
}