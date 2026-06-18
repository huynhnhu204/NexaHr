package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateApiKeyResponse {
    private ApiKeyResponse apiKey;
    private String rawKey;
}
