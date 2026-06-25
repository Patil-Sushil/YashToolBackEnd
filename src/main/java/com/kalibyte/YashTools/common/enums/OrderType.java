package com.kalibyte.YashTools.common.enums;

/**
 * Defines the types of tool orders supported in the system.
 *
 * @author YashTools Development Team
 * @version 2.0
 * @since 2024
 */
public enum OrderType {
    /**
     * New tool manufacturing order
     */
    NEW_TOOL,

    /**
     * Tool resharpening service
     */
    RESHARPENING,

    /**
     * Tool reforming/modification service
     */
    REFORMING;

    /**
     * Normalize enum value for case-insensitive comparison
     */
    public static OrderType fromString(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase().replace(" ", "_"));
    }
}