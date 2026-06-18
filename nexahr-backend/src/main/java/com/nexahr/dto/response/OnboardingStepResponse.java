package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnboardingStepResponse {
    private String key;
    private String title;
    private boolean done;
}
