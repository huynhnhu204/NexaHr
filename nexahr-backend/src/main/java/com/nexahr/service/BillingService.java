package com.nexahr.service;

import com.nexahr.dto.request.CheckoutRequest;
import com.nexahr.dto.response.BillingInvoiceResponse;
import com.nexahr.dto.response.CheckoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillingService {
    CheckoutResponse createCheckoutSession(Long companyId, CheckoutRequest request);
    CheckoutResponse confirmMockCheckout(Long companyId, String sessionId);
    Page<BillingInvoiceResponse> getBillingHistory(Long companyId, Pageable pageable);
    void handleStripeWebhook(String payload, String signature);
}
