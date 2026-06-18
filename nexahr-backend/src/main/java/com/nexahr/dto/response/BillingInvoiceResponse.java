package com.nexahr.dto.response;

import com.nexahr.entity.enums.BillingInvoiceStatus;
import com.nexahr.entity.enums.SubscriptionPlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private SubscriptionPlan plan;
    private BigDecimal amount;
    private String currency;
    private BillingInvoiceStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
