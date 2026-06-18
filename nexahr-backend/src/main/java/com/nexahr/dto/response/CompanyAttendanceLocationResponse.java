package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyAttendanceLocationResponse {
    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;
    private String address;
    private boolean configured;
}
