package com.nexahr.controller;

import com.nexahr.dto.request.UpgradePlanRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.SubscriptionResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.SubscriptionService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ApiResponse<SubscriptionResponse> getSubscription() {
        return ApiResponse.success(subscriptionService.getSubscription(requireCompanyId()));
    }

    @PutMapping("/upgrade")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SubscriptionResponse> upgradePlan(@Valid @RequestBody UpgradePlanRequest request) {
        return ApiResponse.success(
                "Nâng cấp gói thành công",
                subscriptionService.upgradePlan(requireCompanyId(), request.getPlan())
        );
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
