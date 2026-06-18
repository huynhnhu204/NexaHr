package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PushConfigResponse {
    private boolean pushEnabled;
    private String provider;
}
