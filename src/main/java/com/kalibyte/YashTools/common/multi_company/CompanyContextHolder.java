package com.kalibyte.YashTools.common.multi_company;

import java.util.UUID;

public class CompanyContextHolder {
    private static final ThreadLocal<UUID> currentCompanyId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentCompanyCode = new ThreadLocal<>();

    public static void setCompanyId(UUID companyId) {
        currentCompanyId.set(companyId);
    }

    public static UUID getCompanyId() {
        return currentCompanyId.get();
    }

    public static void setCompanyCode(String companyCode) {
        currentCompanyCode.set(companyCode);
    }

    public static String getCompanyCode() {
        return currentCompanyCode.get();
    }

    public static void clear() {
        currentCompanyId.remove();
        currentCompanyCode.remove();
    }
}
