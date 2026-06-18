package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String checkoutUrl;
    private String sessionId;
    private boolean mockMode;
    private String message;
}
