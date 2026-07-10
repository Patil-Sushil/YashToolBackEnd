package com.kalibyte.YashTools.common.enums;

public enum ServiceType {
    RE_SHARPENING,
    RE_FORMING,
    COATING_ONLY;

    public static ServiceType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.equals("COATING") || normalized.equals("COATING_ONLY")) {
            return COATING_ONLY;
        }
        return valueOf(normalized);
    }
}
