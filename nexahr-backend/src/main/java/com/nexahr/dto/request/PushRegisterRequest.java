package com.nexahr.dto.request;

import com.nexahr.entity.enums.PushPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushRegisterRequest {
    @NotBlank
    private String deviceToken;

    @NotNull
    private PushPlatform platform;
}
