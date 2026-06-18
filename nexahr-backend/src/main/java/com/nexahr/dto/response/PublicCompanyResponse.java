package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicCompanyResponse {
    private String name;
    private String code;
    private String logo;
    private String website;
    private String primaryColor;
    private String careersTagline;
    private String address;
}
