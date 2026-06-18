package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private int employeeCount;
    private LocalDateTime createdAt;
}
