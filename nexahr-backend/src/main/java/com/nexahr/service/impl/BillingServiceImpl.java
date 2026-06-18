package com.nexahr.service.impl;

import com.nexahr.dto.request.CheckoutRequest;
import com.nexahr.dto.response.BillingInvoiceResponse;
import com.nexahr.dto.response.CheckoutResponse;
import com.nexahr.entity.BillingInvoice;
import com.nexahr.entity.Company;
import com.nexahr.entity.enums.BillingInvoiceStatus;
import com.nexahr.entity.enums.SubscriptionPlan;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.BillingInvoiceRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.service.BillingService;
import com.nexahr.service.SubscriptionService;
import com.nexahr.util.PlanLimits;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final CompanyRepository companyRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final SubscriptionService subscriptionService;

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @Value("${stripe.enabled:false}")
    private boolean stripeEnabled;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    void initStripe() {
        if (stripeEnabled && stripeApiKey != null && !stripeApiKey.isBlank()) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    @Override
    @Transactional
    public CheckoutResponse createCheckoutSession(Long companyId, CheckoutRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        SubscriptionPlan plan = request.getPlan();
        if (plan == SubscriptionPlan.FREE) {
            throw new BadRequestException("Gói miễn phí không cần thanh toán");
        }

        String invoiceNumber = "INV-" + company.getCode() + "-" + System.currentTimeMillis();
        BillingInvoice invoice = BillingInvoice.builder()
                .company(company)
                .invoiceNumber(invoiceNumber)
                .plan(plan)
                .amount(PlanLimits.getPrice(plan))
                .status(BillingInvoiceStatus.PENDING)
                .build();
        billingInvoiceRepository.save(invoice);

        if (!stripeEnabled || stripeApiKey == null || stripeApiKey.isBlank()) {
            String mockSessionId = "mock_" + UUID.randomUUID();
            invoice.setStripeSessionId(mockSessionId);
            billingInvoiceRepository.save(invoice);
            return CheckoutResponse.builder()
                    .checkoutUrl(frontendUrl + "/settings/subscription?checkout=" + mockSessionId)
                    .sessionId(mockSessionId)
                    .mockMode(true)
                    .message("Chế độ demo — xác nhận thanh toán để nâng cấp gói")
                    .build();
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/settings/subscription?success=true&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/settings/subscription?cancelled=true")
                    .putMetadata("companyId", companyId.toString())
                    .putMetadata("plan", plan.name())
                    .putMetadata("invoiceNumber", invoiceNumber)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("vnd")
                                    .setUnitAmount(PlanLimits.getPrice(plan).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("NexaHR " + plan.name() + " Plan")
                                            .setDescription("Gói đăng ký NexaHR tháng")
                                            .build())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);
            invoice.setStripeSessionId(session.getId());
            billingInvoiceRepository.save(invoice);

            return CheckoutResponse.builder()
                    .checkoutUrl(session.getUrl())
                    .sessionId(session.getId())
                    .mockMode(false)
                    .message("Chuyển đến Stripe để thanh toán")
                    .build();
        } catch (Exception e) {
            log.error("Stripe checkout failed", e);
            invoice.setStatus(BillingInvoiceStatus.FAILED);
            billingInvoiceRepository.save(invoice);
            throw new BadRequestException("Không thể tạo phiên thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CheckoutResponse confirmMockCheckout(Long companyId, String sessionId) {
        BillingInvoice invoice = billingInvoiceRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new BadRequestException("Phiên thanh toán không hợp lệ"));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Phiên thanh toán không thuộc công ty này");
        }

        if (invoice.getStatus() == BillingInvoiceStatus.PAID) {
            return CheckoutResponse.builder()
                    .sessionId(sessionId)
                    .mockMode(true)
                    .message("Thanh toán đã được xác nhận trước đó")
                    .build();
        }

        completePayment(invoice, null);
        return CheckoutResponse.builder()
                .sessionId(sessionId)
                .mockMode(true)
                .message("Thanh toán demo thành công — gói đã được nâng cấp")
                .build();
    }

    @Override
    public Page<BillingInvoiceResponse> getBillingHistory(Long companyId, Pageable pageable) {
        return billingInvoiceRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        if (!stripeEnabled) {
            log.warn("Stripe webhook received but Stripe is disabled");
            return;
        }

        try {
            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (session != null) {
                    billingInvoiceRepository.findByStripeSessionId(session.getId())
                            .ifPresent(invoice -> completePayment(invoice, session.getPaymentIntent()));
                }
            }
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Webhook signature không hợp lệ");
        }
    }

    private void completePayment(BillingInvoice invoice, String paymentIntentId) {
        invoice.setStatus(BillingInvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        if (paymentIntentId != null) {
            invoice.setStripePaymentIntentId(paymentIntentId);
        }
        billingInvoiceRepository.save(invoice);
        subscriptionService.upgradePlan(invoice.getCompany().getId(), invoice.getPlan());
    }

    private BillingInvoiceResponse toResponse(BillingInvoice invoice) {
        return BillingInvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .plan(invoice.getPlan())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
