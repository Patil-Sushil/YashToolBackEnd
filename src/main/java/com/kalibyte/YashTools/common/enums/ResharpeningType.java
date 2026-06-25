package com.kalibyte.YashTools.common.enums;

/**
 * Classification of resharpening service levels
 */
public enum ResharpeningType {
    /**
     * Basic edge resharpening only
     */
    PRIMARY,

    /**
     * Advanced resharpening with minor corrections
     */
    SECONDARY,

    /**
     * Complete resharpening with full geometry restoration
     */
    FULL;
}