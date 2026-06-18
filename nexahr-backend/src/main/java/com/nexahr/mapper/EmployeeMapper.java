package com.nexahr.mapper;

import com.nexahr.entity.*;
import com.nexahr.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) return null;
        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .email(employee.getUser() != null ? employee.getUser().getEmail() : null)
                .gender(employee.getGender())
                .dateOfBirth(employee.getDateOfBirth())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .avatar(employee.getAvatar())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
                .positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
                .hireDate(employee.getHireDate())
                .employmentStatus(employee.getEmploymentStatus())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .nationalId(employee.getNationalId())
                .personalEmail(employee.getPersonalEmail())
                .contractType(employee.getContractType())
                .contractStartDate(employee.getContractStartDate())
                .contractEndDate(employee.getContractEndDate())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFullName() : null)
                .createdAt(employee.getCreatedAt())
                .build();
    }

    public DepartmentResponse toDepartmentResponse(Department dept) {
        if (dept == null) return null;
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .managerId(dept.getManager() != null ? dept.getManager().getId() : null)
                .managerName(dept.getManager() != null ? dept.getManager().getFullName() : null)
                .employeeCount(dept.getEmployees() != null ? dept.getEmployees().size() : 0)
                .createdAt(dept.getCreatedAt())
                .build();
    }

    public AttendanceResponse toAttendanceResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .companyId(a.getCompany() != null ? a.getCompany().getId()
                        : (a.getEmployee().getCompany() != null ? a.getEmployee().getCompany().getId() : null))
                .employeeName(a.getEmployee().getFullName())
                .workDate(a.getWorkDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .checkInPhotoUrl(a.getCheckInPhotoUrl())
                .checkOutPhotoUrl(a.getCheckOutPhotoUrl())
                .checkInLatitude(a.getCheckInLatitude())
                .checkInLongitude(a.getCheckInLongitude())
                .checkOutLatitude(a.getCheckOutLatitude())
                .checkOutLongitude(a.getCheckOutLongitude())
                .checkInDistanceMeters(a.getCheckInDistanceMeters())
                .checkOutDistanceMeters(a.getCheckOutDistanceMeters())
                .checkInAddress(a.getCheckInAddress())
                .checkOutAddress(a.getCheckOutAddress())
                .totalHours(a.getTotalHours())
                .status(a.getStatus())
                .note(a.getNote())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public LeaveResponse toLeaveResponse(com.nexahr.entity.LeaveRequest lr) {
        return LeaveResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFullName())
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .totalDays(lr.getTotalDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .approvedByName(lr.getApprovedBy() != null ? lr.getApprovedBy().getUsername() : null)
                .approvedAt(lr.getApprovedAt())
                .rejectReason(lr.getRejectReason())
                .createdAt(lr.getCreatedAt())
                .build();
    }

    public PayrollResponse toPayrollResponse(Payroll p) {
        return PayrollResponse.builder()
                .id(p.getId())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getEmployee().getFullName())
                .employeeCode(p.getEmployee().getEmployeeCode())
                .departmentName(p.getEmployee().getDepartment() != null ? p.getEmployee().getDepartment().getName() : null)
                .salaryMonth(p.getSalaryMonth())
                .baseSalary(p.getBaseSalary())
                .allowance(p.getAllowance())
                .bonus(p.getBonus())
                .deduction(p.getDeduction())
                .workingDays(p.getWorkingDays())
                .standardWorkingDays(p.getStandardWorkingDays())
                .actualWorkingDays(p.getActualWorkingDays())
                .overtimeHours(p.getOvertimeHours())
                .overtimePay(p.getOvertimePay())
                .socialInsurance(p.getSocialInsurance())
                .healthInsurance(p.getHealthInsurance())
                .unemploymentInsurance(p.getUnemploymentInsurance())
                .personalIncomeTax(p.getPersonalIncomeTax())
                .grossIncome(p.getGrossIncome())
                .totalDeduction(p.getTotalDeduction())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .build();
    }
}
