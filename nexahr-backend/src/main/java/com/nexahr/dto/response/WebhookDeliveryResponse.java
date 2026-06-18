package com.nexahr.dto.response;

import com.nexahr.entity.enums.WebhookEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WebhookDeliveryResponse {
    private Long id;
    private Long webhookId;
    private String webhookName;
    private WebhookEvent event;
    private Integer statusCode;
    private boolean success;
    private LocalDateTime attemptedAt;
}
