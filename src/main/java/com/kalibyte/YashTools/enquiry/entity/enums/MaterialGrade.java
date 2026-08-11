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
    ASP30("ASP30 - Premium PM HSS"),

    // New coolant hole grades
    TWO_THREE_HOLE_K40_330("2 Hole/3 Hole,30 Degree/40 Degree, Grade K-40,330mm"),
    CENTRAL_PARALLEL_K40_330("Central / Parallel hole, Grade K-40, Length 330mm"),

    // New Net Price grades
    K40UF_H10F("k40ufH10f"),
    AM70_DM80("am70Dm80"),
    PN90("pn90"),
    GP10_K10F("gp10K10f");

    private final String description;

    MaterialGrade(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    public static MaterialGrade fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim();
        String normalized = clean.replace("/", "_").replace("-", "_").replace(" ", "_").toUpperCase();
        for (MaterialGrade grade : MaterialGrade.values()) {
            if (grade.name().equalsIgnoreCase(clean) ||
                grade.name().equalsIgnoreCase(normalized) ||
                grade.getDescription().equalsIgnoreCase(clean)) {
                return grade;
            }
        }
        return K40UF_H10F;
    }
}
