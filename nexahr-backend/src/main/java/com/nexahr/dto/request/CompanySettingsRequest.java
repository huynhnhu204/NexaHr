package com.nexahr.dto.request;

import com.nexahr.entity.enums.DataRegion;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompanySettingsRequest {
    private String name;
    private String logo;
    private String address;
    private String phone;
    private String website;
    private String billingEmail;
    private String careersTagline;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Màu chính phải là mã hex hợp lệ")
    private String primaryColor;

    private String timezone;
    private String locale;
    private DataRegion dataRegion;
    private Double latitude;
    private Double longitude;
    private Integer attendanceRadiusMeters;
}
