package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SamlConfigResponse {
    private boolean enabled;
    private String idpName;
    private String entityId;
    private String ssoUrl;
    private String certificate;
    private String attributeEmail;
    private String metadataUrl;
    private String acsUrl;
    private boolean enterpriseRequired;
}
