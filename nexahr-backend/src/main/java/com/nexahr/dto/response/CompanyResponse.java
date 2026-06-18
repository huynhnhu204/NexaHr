package com.nexahr.dto.response;

import com.nexahr.entity.enums.CompanyStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String code;
    private String logo;
    private String address;
    private String plan;
    private CompanyStatus status;
    private boolean onboardingCompleted;
    private boolean isDefault;
}
