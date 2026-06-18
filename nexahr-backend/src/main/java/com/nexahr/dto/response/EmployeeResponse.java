package com.nexahr.dto.response;

import com.nexahr.entity.enums.EmploymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String avatar;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionName;
    private LocalDate hireDate;
    private EmploymentStatus employmentStatus;
    private Integer annualLeaveBalance;
    private String nationalId;
    private String personalEmail;
    private String contractType;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private Long managerId;
    private String managerName;
    private LocalDateTime createdAt;
}
