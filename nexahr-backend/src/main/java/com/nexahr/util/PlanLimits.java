package com.nexahr.util;

import com.nexahr.entity.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlanLimits {

    private PlanLimits() {
    }

    public static int getMaxEmployees(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 10;
            case PRO -> 100;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    public static BigDecimal getPrice(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> BigDecimal.ZERO;
            case PRO -> BigDecimal.valueOf(999_000);
            case ENTERPRISE -> BigDecimal.valueOf(4_999_000);
        };
    }

    public static Map<String, String> getFeatures(SubscriptionPlan plan) {
        Map<String, String> features = new LinkedHashMap<>();
        switch (plan) {
            case FREE -> {
                features.put("employees", "Tối đa 10 nhân viên");
                features.put("departments", "Quản lý phòng ban cơ bản");
                features.put("support", "Hỗ trợ qua email");
            }
            case PRO -> {
                features.put("employees", "Tối đa 100 nhân viên");
                features.put("departments", "Quản lý phòng ban nâng cao");
                features.put("recruitment", "Tuyển dụng & careers portal");
                features.put("analytics", "Báo cáo & phân tích");
                features.put("support", "Hỗ trợ ưu tiên");
            }
            case ENTERPRISE -> {
                features.put("employees", "Không giới hạn nhân viên");
                features.put("departments", "Quản lý đa chi nhánh");
                features.put("recruitment", "Tuyển dụng & careers portal");
                features.put("analytics", "Báo cáo & phân tích nâng cao");
                features.put("integrations", "Tích hợp API & SSO");
                features.put("support", "Hỗ trợ 24/7");
            }
        }
        return features;
    }
}
