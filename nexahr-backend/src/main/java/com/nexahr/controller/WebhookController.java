package com.nexahr.controller;

import com.nexahr.dto.request.WebhookEndpointRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.WebhookDeliveryResponse;
import com.nexahr.dto.response.WebhookEndpointResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.WebhookService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    public ApiResponse<List<WebhookEndpointResponse>> list() {
        return ApiResponse.success(webhookService.listEndpoints(requireCompanyId()));
    }

    @PostMapping
    public ApiResponse<WebhookEndpointResponse> create(@Valid @RequestBody WebhookEndpointRequest request) {
        return ApiResponse.success("Tạo webhook thành công",
                webhookService.createEndpoint(requireCompanyId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        webhookService.deleteEndpoint(requireCompanyId(), id);
        return ApiResponse.success("Đã xóa webhook", null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Void> test(@PathVariable Long id) {
        webhookService.testEndpoint(requireCompanyId(), id);
        return ApiResponse.success("Đã gửi webhook test", null);
    }

    @GetMapping("/deliveries")
    public ApiResponse<Page<WebhookDeliveryResponse>> deliveries(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(webhookService.getDeliveries(requireCompanyId(), pageable));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
