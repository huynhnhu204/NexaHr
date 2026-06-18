package com.nexahr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApiKeyRequest {
    @NotBlank
    private String name;
    private String scopes;
}
