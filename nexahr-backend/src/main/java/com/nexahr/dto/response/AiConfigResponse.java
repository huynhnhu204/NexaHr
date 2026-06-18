package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiConfigResponse {
    private boolean llmEnabled;
    private String model;
    private String provider;
}
