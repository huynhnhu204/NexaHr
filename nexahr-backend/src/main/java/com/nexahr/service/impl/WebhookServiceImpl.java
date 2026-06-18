package com.nexahr.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexahr.dto.request.WebhookEndpointRequest;
import com.nexahr.dto.response.WebhookDeliveryResponse;
import com.nexahr.dto.response.WebhookEndpointResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.WebhookDelivery;
import com.nexahr.entity.WebhookEndpoint;
import com.nexahr.entity.enums.WebhookEvent;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.WebhookDeliveryRepository;
import com.nexahr.repository.WebhookEndpointRepository;
import com.nexahr.service.WebhookService;
import com.nexahr.util.PlanFeatureGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<WebhookEndpointResponse> listEndpoints(Long companyId) {
        return webhookEndpointRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WebhookEndpointResponse createEndpoint(Long companyId, WebhookEndpointRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        PlanFeatureGate.requireProOrEnterprise(company);

        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .company(company)
                .name(request.getName())
                .url(request.getUrl())
                .secret(generateSecret())
                .events(String.join(",", request.getEvents()))
                .build();
        webhookEndpointRepository.save(endpoint);
        return toResponse(endpoint);
    }

    @Override
    @Transactional
    public void deleteEndpoint(Long companyId, Long endpointId) {
        WebhookEndpoint endpoint = webhookEndpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy webhook"));
        if (!endpoint.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy webhook");
        }
        endpoint.setActive(false);
        webhookEndpointRepository.save(endpoint);
    }

    @Override
    @Transactional
    public void testEndpoint(Long companyId, Long endpointId) {
        WebhookEndpoint endpoint = webhookEndpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy webhook"));
        if (!endpoint.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy webhook");
        }
        Map<String, Object> payload = Map.of(
                "event", "test.ping",
                "timestamp", Instant.now().toString(),
                "message", "NexaHR webhook test"
        );
        deliver(endpoint, WebhookEvent.EMPLOYEE_CREATED, payload);
    }

    @Override
    public Page<WebhookDeliveryResponse> getDeliveries(Long companyId, Pageable pageable) {
        return webhookDeliveryRepository
                .findByWebhookEndpointCompanyIdOrderByAttemptedAtDesc(companyId, pageable)
                .map(this::toDeliveryResponse);
    }

    @Override
    public void dispatch(Long companyId, WebhookEvent event, Map<String, Object> payload) {
        List<WebhookEndpoint> endpoints = webhookEndpointRepository.findByCompanyIdAndActiveTrue(companyId);
        for (WebhookEndpoint endpoint : endpoints) {
            if (subscribesTo(endpoint, event)) {
                try {
                    deliver(endpoint, event, payload);
                } catch (Exception e) {
                    log.warn("Webhook delivery failed for endpoint {}: {}", endpoint.getId(), e.getMessage());
                }
            }
        }
    }

    private void deliver(WebhookEndpoint endpoint, WebhookEvent event, Map<String, Object> payload) {
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            body = payload.toString();
        }

        String signature = sign(endpoint.getSecret(), body);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NexaHR-Signature", signature);
        headers.set("X-NexaHR-Event", event.name());

        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhookEndpoint(endpoint)
                .event(event)
                .payload(body)
                .build();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.getUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            delivery.setStatusCode(response.getStatusCode().value());
            delivery.setResponseBody(truncate(response.getBody(), 500));
            delivery.setSuccess(response.getStatusCode().is2xxSuccessful());
        } catch (Exception e) {
            delivery.setStatusCode(0);
            delivery.setResponseBody(truncate(e.getMessage(), 500));
            delivery.setSuccess(false);
        }

        webhookDeliveryRepository.save(delivery);
    }

    private boolean subscribesTo(WebhookEndpoint endpoint, WebhookEvent event) {
        return Arrays.asList(endpoint.getEvents().split(","))
                .stream()
                .map(String::trim)
                .anyMatch(e -> e.equals(event.name()) || e.equals("*"));
    }

    private String sign(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    private WebhookEndpointResponse toResponse(WebhookEndpoint endpoint) {
        return WebhookEndpointResponse.builder()
                .id(endpoint.getId())
                .name(endpoint.getName())
                .url(endpoint.getUrl())
                .events(Arrays.asList(endpoint.getEvents().split(",")))
                .active(endpoint.isActive())
                .createdAt(endpoint.getCreatedAt())
                .build();
    }

    private WebhookDeliveryResponse toDeliveryResponse(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .webhookId(delivery.getWebhookEndpoint().getId())
                .webhookName(delivery.getWebhookEndpoint().getName())
                .event(delivery.getEvent())
                .statusCode(delivery.getStatusCode())
                .success(delivery.isSuccess())
                .attemptedAt(delivery.getAttemptedAt())
                .build();
    }
}
