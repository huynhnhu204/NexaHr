package com.nexahr.service;

import com.nexahr.dto.request.WebhookEndpointRequest;
import com.nexahr.dto.response.WebhookDeliveryResponse;
import com.nexahr.dto.response.WebhookEndpointResponse;
import com.nexahr.entity.enums.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface WebhookService {
    List<WebhookEndpointResponse> listEndpoints(Long companyId);
    WebhookEndpointResponse createEndpoint(Long companyId, WebhookEndpointRequest request);
    void deleteEndpoint(Long companyId, Long endpointId);
    void testEndpoint(Long companyId, Long endpointId);
    Page<WebhookDeliveryResponse> getDeliveries(Long companyId, Pageable pageable);
    void dispatch(Long companyId, WebhookEvent event, Map<String, Object> payload);
}
