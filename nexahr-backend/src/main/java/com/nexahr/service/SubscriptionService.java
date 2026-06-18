package com.nexahr.service;

import com.nexahr.dto.request.UpgradePlanRequest;
import com.nexahr.dto.response.SubscriptionResponse;
import com.nexahr.entity.enums.SubscriptionPlan;

public interface SubscriptionService {
    SubscriptionResponse getSubscription(Long companyId);
    SubscriptionResponse upgradePlan(Long companyId, SubscriptionPlan plan);
}
