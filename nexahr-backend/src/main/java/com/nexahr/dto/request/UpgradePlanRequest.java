package com.nexahr.dto.request;

import com.nexahr.entity.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradePlanRequest {
    @NotNull(message = "Gói đăng ký không được để trống")
    private SubscriptionPlan plan;
}
