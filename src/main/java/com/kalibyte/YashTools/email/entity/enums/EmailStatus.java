package com.kalibyte.YashTools.email.entity.enums;

public enum EmailStatus {
    PENDING, PROCESSING, SENT, DELIVERED, FAILED, BOUNCED, REJECTED;

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == BOUNCED || this == REJECTED;
    }

    public boolean canRetry() {
        return this == FAILED;
    }
}