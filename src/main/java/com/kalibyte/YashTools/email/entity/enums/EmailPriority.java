package com.kalibyte.YashTools.email.entity.enums;

public enum EmailPriority {
    LOW(1), NORMAL(2), HIGH(3), URGENT(4);

    private final int rank;
    EmailPriority(int rank) { this.rank = rank; }
    public int getRank() { return rank; }
}