package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicJobDetailResponse {
    private Long id;
    private String title;
    private String departmentName;
    private String positionName;
    private String description;
    private String requirement;
    private String salaryRange;
    private LocalDateTime createdAt;
}
