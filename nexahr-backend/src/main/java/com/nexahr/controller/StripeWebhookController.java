package com.nexahr.controller;

import com.nexahr.exception.BadRequestException;
import com.nexahr.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final BillingService billingService;

    @PostMapping("/stripe")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        if (signature == null) {
            throw new BadRequestException("Thiếu Stripe-Signature header");
        }
        billingService.handleStripeWebhook(payload, signature);
    }
}
