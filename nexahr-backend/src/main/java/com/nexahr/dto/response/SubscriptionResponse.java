package com.nexahr.dto.response;

import com.nexahr.entity.enums.SubscriptionPlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SubscriptionResponse {
    private SubscriptionPlan plan;
    private int maxEmployees;
    private long currentEmployees;
    private double usagePercent;
    private BigDecimal price;
    private List<String> features;
    private String billingEmail;
    private LocalDate nextBillingDate;
}
