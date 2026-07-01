package com.kalibyte.YashTools.email.constants;

public final class EmailConstants {
    private EmailConstants() {}

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String PROVIDER_SMTP = "SMTP";

    public static final int MAX_RETRIES = 3;
    public static final long MAX_ATTACHMENT_SIZE_BYTES = 10 * 1024 * 1024;
    public static final long MAX_TOTAL_ATTACHMENT_SIZE = 25 * 1024 * 1024;
    public static final int MAX_RECIPIENTS_PER_EMAIL = 50;

    public static final int SMTP_CONNECTION_TIMEOUT_MS = 30_000;
    public static final int SMTP_TIMEOUT_MS = 30_000;
    public static final int SMTP_WRITE_TIMEOUT_MS = 30_000;

    public static final long RETRY_BACKOFF_BASE_SECONDS = 60;
    public static final long RETRY_BACKOFF_MULTIPLIER = 2;

    public static final String OWNER_TYPE_QUOTATION = "QUOTATION";
    public static final String OWNER_TYPE_INVOICE = "INVOICE";
    public static final String OWNER_TYPE_DELIVERY_CHALLAN = "DELIVERY_CHALLAN";

    public static final String HEADER_X_ENTITY_TYPE = "X-YashTools-Entity-Type";
    public static final String HEADER_X_ENTITY_ID = "X-YashTools-Entity-Id";
}