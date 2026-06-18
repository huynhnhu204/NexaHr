package com.nexahr.dto.response;

import com.nexahr.entity.enums.DataRegion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanySettingsResponse {
    private Long id;
    private String name;
    private String code;
    private String logo;
    private String address;
    private String phone;
    private String website;
    private String billingEmail;
    private String primaryColor;
    private String careersTagline;
    private String plan;
    private String timezone;
    private String locale;
    private DataRegion dataRegion;
    private Double latitude;
    private Double longitude;
    private Integer attendanceRadiusMeters;
}
