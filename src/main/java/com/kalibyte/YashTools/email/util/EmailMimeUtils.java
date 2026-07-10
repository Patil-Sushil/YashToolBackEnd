package com.kalibyte.YashTools.email.util;

import java.util.Locale;

public final class EmailMimeUtils {
    private EmailMimeUtils() {}

    public static String mimeType(String filename) {
        if (filename == null) return "application/octet-stream";
        String l = filename.toLowerCase(Locale.ROOT);
        if (l.endsWith(".pdf")) return "application/pdf";
        if (l.endsWith(".png")) return "image/png";
        if (l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
        if (l.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (l.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }
}