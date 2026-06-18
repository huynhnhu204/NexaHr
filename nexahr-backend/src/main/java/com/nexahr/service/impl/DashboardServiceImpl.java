package com.nexahr.service.impl;

import com.nexahr.dto.response.DashboardSummaryResponse;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.JobStatus;
import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.*;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl {

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPostingRepository jobPostingRepository;

    public DashboardSummaryResponse getSummary() {
        Long companyId = requireCompanyId();
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate monthStart = YearMonth.now().atDay(1);

        return DashboardSummaryResponse.builder()
                .totalEmployees(employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.ACTIVE, companyId)
                        + employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.PROBATION, companyId))
                .newEmployeesThisMonth(employeeRepository.countNewEmployeesSince(companyId, monthStart))
                .pendingLeaveRequests(leaveRepository.countByStatusAndCompanyId(LeaveStatus.PENDING, companyId))
                .totalPayrollThisMonth(payrollRepository.sumNetSalaryByMonthAndCompanyId(currentMonth, companyId))
                .activeRecruitment(jobPostingRepository.countByStatusAndCompanyId(JobStatus.OPEN, companyId))
                .openPositions(jobPostingRepository.countByStatusAndCompanyId(JobStatus.OPEN, companyId))
                .recentActivities(null)
                .build();
    }

    public List<Map<String, Object>> getEmployeeChart() {
        List<Object[]> data = departmentRepository.countEmployeesByDepartment(requireCompanyId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("name", row[0], "value", row[1]));
        }
        return result;
    }

    public List<Map<String, Object>> getPayrollChart() {
        List<Object[]> data = payrollRepository.sumSalaryByMonthAndCompanyId(requireCompanyId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("month", row[0], "amount", row[1]));
        }
        if (result.isEmpty()) {
            result.add(Map.of("month", YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    "amount", BigDecimal.ZERO));
        }
        return result;
    }

    public List<Map<String, Object>> getRecruitmentChart() {
        List<Object[]> data = jobPostingRepository.countByMonthAndCompanyId(requireCompanyId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("month", row[0], "count", row[1]));
        }
        return result;
    }

    public List<Map<String, Object>> getLeaveChart() {
        List<Object[]> data = leaveRepository.countByStatusGroupAndCompanyId(requireCompanyId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("status", row[0].toString(), "count", row[1]));
        }
        return result;
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
