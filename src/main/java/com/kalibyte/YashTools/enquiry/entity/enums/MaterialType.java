package com.kalibyte.YashTools.enquiry.entity.enums;

/**
 * Base material types used in tool manufacturing
 *
 * @apiNote Only applicable for NEW_TOOL order type
 */
public enum MaterialType {
    /**
     * High-Speed Steel - General purpose cutting tools
     */
    HSS("High-Speed Steel"),

    /**
     * Carbide - For high-performance machining
     */
    CARBIDE("Carbide"),

    /**
     * Cobalt High-Speed Steel - Enhanced heat resistance
     */
    COBALT_HSS("Cobalt HSS"),

    /**
     * Powder Metallurgy High-Speed Steel
     */
    PM_HSS("Powder Metallurgy HSS"),

    /**
     * Tool Steel - For special applications
     */
    TOOL_STEEL("Tool Steel");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}