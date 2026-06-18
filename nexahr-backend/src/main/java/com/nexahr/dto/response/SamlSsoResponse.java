package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SamlSsoResponse {
    private boolean enabled;
    private String companyCode;
    private String companyName;
    private String ssoUrl;
    private boolean demoMode;
}
