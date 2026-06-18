package com.nexahr.controller;

import com.nexahr.dto.request.OnboardingStepRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.OnboardingStatusResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.OnboardingService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/status")
    public ApiResponse<OnboardingStatusResponse> getStatus() {
        return ApiResponse.success(onboardingService.getStatus(requireCompanyId()));
    }

    @PutMapping("/step")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<OnboardingStatusResponse> updateStep(@Valid @RequestBody OnboardingStepRequest request) {
        return ApiResponse.success(
                "Cập nhật bước onboarding thành công",
                onboardingService.updateStep(requireCompanyId(), request.getStep())
        );
    }

    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<OnboardingStatusResponse> complete() {
        return ApiResponse.success(
                "Hoàn tất onboarding thành công",
                onboardingService.complete(requireCompanyId())
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
