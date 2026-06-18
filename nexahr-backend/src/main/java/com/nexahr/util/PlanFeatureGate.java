package com.nexahr.util;

import com.nexahr.entity.Company;
import com.nexahr.entity.enums.SubscriptionPlan;
import com.nexahr.exception.BadRequestException;

public final class PlanFeatureGate {

    private PlanFeatureGate() {
    }

    public static void requireEnterprise(Company company) {
        SubscriptionPlan plan = resolvePlan(company.getPlan());
        if (plan != SubscriptionPlan.ENTERPRISE) {
            throw new BadRequestException("Tính năng này chỉ khả dụng với gói Enterprise");
        }
    }

    public static void requireProOrEnterprise(Company company) {
        SubscriptionPlan plan = resolvePlan(company.getPlan());
        if (plan == SubscriptionPlan.FREE) {
            throw new BadRequestException("Tính năng này yêu cầu gói Pro hoặc Enterprise");
        }
    }

    private static SubscriptionPlan resolvePlan(String plan) {
        try {
            return SubscriptionPlan.valueOf(plan);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SubscriptionPlan.FREE;
        }
    }
}
