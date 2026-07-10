package com.kalibyte.YashTools.quotation.entity.enums;

public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN;

    public boolean isPending() { return this == PENDING; }
    public boolean isApproved() { return this == APPROVED; }
}