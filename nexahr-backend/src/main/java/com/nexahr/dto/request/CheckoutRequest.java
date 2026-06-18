package com.nexahr.dto.request;

import com.nexahr.entity.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotNull
    private SubscriptionPlan plan;
}
