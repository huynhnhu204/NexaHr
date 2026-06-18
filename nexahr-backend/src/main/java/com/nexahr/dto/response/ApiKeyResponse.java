package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String keyPrefix;
    private String scopes;
    private boolean active;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
