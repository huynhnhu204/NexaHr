package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OnboardingStatusResponse {
    private boolean completed;
    private int currentStep;
    private int totalSteps;
    private List<OnboardingStepResponse> steps;
}
