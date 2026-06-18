package com.nexahr.dto.request;

import com.nexahr.entity.enums.EmploymentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;
    private String username;
    private String password;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Long departmentId;
    private Long positionId;
    private LocalDate hireDate;
    private EmploymentStatus employmentStatus;
    private String employeeCode;
    private String nationalId;
    private String personalEmail;
    private String contractType;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private Long managerId;
}
