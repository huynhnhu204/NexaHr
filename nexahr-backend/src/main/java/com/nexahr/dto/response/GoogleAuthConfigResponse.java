package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoogleAuthConfigResponse {
    private boolean enabled;
    private String clientId;
}
