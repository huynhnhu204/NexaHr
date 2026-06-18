package com.nexahr.tenant;

public final class TenantContext {

    private static final ThreadLocal<Long> COMPANY_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getCompanyId() {
        return COMPANY_ID.get();
    }

    public static void setCompanyId(Long companyId) {
        COMPANY_ID.set(companyId);
    }

    public static void clear() {
        COMPANY_ID.remove();
    }
}
