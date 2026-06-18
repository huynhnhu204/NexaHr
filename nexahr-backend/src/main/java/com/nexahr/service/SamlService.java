package com.nexahr.service;

import com.nexahr.dto.request.SamlConfigRequest;
import com.nexahr.dto.request.SamlDemoLoginRequest;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.SamlConfigResponse;
import com.nexahr.dto.response.SamlSsoResponse;

public interface SamlService {
    SamlConfigResponse getConfig(Long companyId);
    SamlConfigResponse updateConfig(Long companyId, SamlConfigRequest request);
    String getMetadata(String companyCode);
    AuthResponse demoLogin(SamlDemoLoginRequest request);
    SamlSsoResponse getSsoInit(String companyCode);
}
