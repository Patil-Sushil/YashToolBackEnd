package com.kalibyte.YashTools.enquiry.entity.enums;
/**
 * Standard material grade classifications
 *
 * @see <a href="https://www.astm.org">ASTM Standards</a>
 */
public enum MaterialGrade {
    // HSS Grades
    /**
     * Molybdenum HSS - General purpose
     */
    M2("M2 - Molybdenum HSS"),

    /**
     * Cobalt HSS - High heat resistance
     */
    M35("M35 - 5% Cobalt HSS"),

    /**
     * Premium Cobalt HSS - Superior performance
     */
    M42("M42 - 8% Cobalt HSS"),

    // Carbide Grades
    /**
     * Fine grain carbide - Finishing operations
     */
    K10("K10 - Fine Grain Carbide"),

    /**
     * Medium grain carbide - General machining
     */
    K20("K20 - Medium Grain Carbide"),

    /**
     * Coarse grain carbide - Roughing operations
     */
    K30("K30 - Coarse Grain Carbide"),

    // Tool Steel Grades
    /**
     * Oil-hardening tool steel
     */
    O1("O1 - Oil Hardening Tool Steel"),

    /**
     * Air-hardening tool steel
     */
    A2("A2 - Air Hardening Tool Steel"),

    /**
     * High-carbon, high-chromium tool steel
     */
    D2("D2 - High Carbon Tool Steel"),

    // PM Grades
    /**
     * Powder metallurgy high-speed steel
     */
    ASP23("ASP23 - PM HSS"),

    /**
     * Advanced powder metallurgy steel
     */
    ASP30("ASP30 - Premium PM HSS");

    private final String description;

    MaterialGrade(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
