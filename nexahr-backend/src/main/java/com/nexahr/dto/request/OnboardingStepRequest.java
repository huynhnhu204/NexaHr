package com.nexahr.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OnboardingStepRequest {
    @NotNull(message = "Bước onboarding không được để trống")
    @Min(value = 1, message = "Bước onboarding phải từ 1 đến 4")
    @Max(value = 4, message = "Bước onboarding phải từ 1 đến 4")
    private Integer step;
}
