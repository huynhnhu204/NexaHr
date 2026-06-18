package com.nexahr.entity;

import com.nexahr.entity.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payrolls", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "salary_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "salary_month", nullable = false)
    private String salaryMonth;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal bonus;

    private BigDecimal deduction;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "standard_working_days")
    private Integer standardWorkingDays;

    @Column(name = "actual_working_days")
    private Integer actualWorkingDays;

    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours;

    @Column(name = "overtime_pay")
    private BigDecimal overtimePay;

    @Column(name = "social_insurance")
    private BigDecimal socialInsurance;

    @Column(name = "health_insurance")
    private BigDecimal healthInsurance;

    @Column(name = "unemployment_insurance")
    private BigDecimal unemploymentInsurance;

    @Column(name = "personal_income_tax")
    private BigDecimal personalIncomeTax;

    @Column(name = "gross_income")
    private BigDecimal grossIncome;

    @Column(name = "total_deduction")
    private BigDecimal totalDeduction;

    @Column(name = "net_salary")
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayrollStatus status = PayrollStatus.DRAFT;
}
