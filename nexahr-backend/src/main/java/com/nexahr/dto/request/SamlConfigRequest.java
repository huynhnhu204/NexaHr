package com.nexahr.dto.request;

import lombok.Data;

@Data
public class SamlConfigRequest {
    private Boolean enabled;
    private String idpName;
    private String entityId;
    private String ssoUrl;
    private String certificate;
    private String attributeEmail;
}
