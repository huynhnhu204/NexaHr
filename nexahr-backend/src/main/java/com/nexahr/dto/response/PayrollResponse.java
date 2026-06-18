package com.nexahr.dto.response;

import com.nexahr.entity.enums.PayrollStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayrollResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private Integer workingDays;
    private Integer standardWorkingDays;
    private Integer actualWorkingDays;
    private BigDecimal overtimeHours;
    private BigDecimal overtimePay;
    private BigDecimal socialInsurance;
    private BigDecimal healthInsurance;
    private BigDecimal unemploymentInsurance;
    private BigDecimal personalIncomeTax;
    private BigDecimal grossIncome;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;
    private PayrollStatus status;
}
