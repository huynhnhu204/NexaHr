package com.nexahr.service;

import com.nexahr.dto.response.OnboardingStatusResponse;

public interface OnboardingService {
    OnboardingStatusResponse getStatus(Long companyId);
    OnboardingStatusResponse updateStep(Long companyId, int step);
    OnboardingStatusResponse complete(Long companyId);
}
