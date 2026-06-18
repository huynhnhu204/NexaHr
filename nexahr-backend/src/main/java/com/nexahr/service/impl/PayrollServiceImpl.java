package com.nexahr.service.impl;

import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.PayrollResponse;
import com.nexahr.entity.Employee;
import com.nexahr.entity.Notification;
import com.nexahr.entity.Payroll;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.NotificationType;
import com.nexahr.entity.enums.PayrollStatus;
import com.nexahr.entity.enums.Role;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.mapper.EmployeeMapper;
import com.nexahr.repository.AttendanceRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.repository.NotificationRepository;
import com.nexahr.repository.PayrollRepository;
import com.nexahr.service.PayrollService;
import com.nexahr.tenant.TenantContext;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private static final BigDecimal SOCIAL_INSURANCE_RATE = new BigDecimal("0.08");
    private static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.015");
    private static final BigDecimal UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.01");
    private static final BigDecimal PERSONAL_INCOME_TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal INSURANCE_SALARY_CAP = new BigDecimal("36000000");

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeMapper mapper;

    @Override
    public PageResponse<PayrollResponse> getAll(String month, Pageable pageable) {
        Long companyId = requireCompanyId();
        if (month != null) {
            List<Payroll> payrolls = payrollRepository.findBySalaryMonthAndCompanyId(month, companyId);
            List<PayrollResponse> responses = payrolls.stream().map(mapper::toPayrollResponse).toList();
            return PageResponse.<PayrollResponse>builder()
                    .content(responses)
                    .page(0)
                    .size(responses.size())
                    .totalElements(responses.size())
                    .totalPages(1)
                    .last(true)
                    .build();
        }
        return PageUtil.toPageResponse(payrollRepository.findByCompanyId(companyId, pageable)
                .map(mapper::toPayrollResponse));
    }

    @Override
    public PageResponse<PayrollResponse> getMy(Long employeeId, Pageable pageable) {
        return PageUtil.toPageResponse(payrollRepository.findByEmployeeId(employeeId, pageable)
                .map(mapper::toPayrollResponse));
    }

    @Override
    public PayrollResponse getById(Long id) {
        return mapper.toPayrollResponse(findPayrollInTenant(id));
    }

    @Override
    public PayrollResponse getByIdForUser(Long id, User user) {
        Payroll payroll = findPayrollInTenant(id);
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.HR) {
            return mapper.toPayrollResponse(payroll);
        }
        Employee requester = user.getEmployee();
        if (requester == null || !payroll.getEmployee().getId().equals(requester.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền xem bảng lương này");
        }
        return mapper.toPayrollResponse(payroll);
    }

    @Override
    @Transactional
    public List<PayrollResponse> generate(String month) {
        if (month == null) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        Long companyId = requireCompanyId();
        List<Employee> employees = employeeRepository.findActiveByCompanyId(companyId);

        String finalMonth = month;
        return employees.stream().map(emp -> generateForEmployee(emp, finalMonth)).toList();
    }

    @Override
    @Transactional
    public PayrollResponse approve(Long id) {
        Payroll payroll = findPayrollInTenant(id);
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể phê duyệt bảng lương ở trạng thái DRAFT");
        }
        payroll.setStatus(PayrollStatus.APPROVED);
        payrollRepository.save(payroll);

        if (payroll.getEmployee().getUser() != null) {
            notificationRepository.save(Notification.builder()
                    .user(payroll.getEmployee().getUser())
                    .title("Bảng lương đã được phê duyệt")
                    .message("Bảng lương tháng " + payroll.getSalaryMonth() + " đã được phê duyệt.")
                    .type(NotificationType.PAYROLL_PUBLISHED)
                    .isRead(false)
                    .build());
        }

        return mapper.toPayrollResponse(payroll);
    }

    @Override
    @Transactional
    public PayrollResponse markPaid(Long id) {
        Payroll payroll = findPayrollInTenant(id);
        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể đánh dấu đã thanh toán bảng lương đã phê duyệt");
        }
        payroll.setStatus(PayrollStatus.PAID);
        return mapper.toPayrollResponse(payrollRepository.save(payroll));
    }

    public Payroll findPayrollInTenant(Long id) {
        return payrollRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng lương"));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }

    private PayrollResponse generateForEmployee(Employee employee, String month) {
        var existing = payrollRepository.findByEmployeeIdAndSalaryMonth(employee.getId(), month);
        if (existing.isPresent()) {
            return mapper.toPayrollResponse(existing.get());
        }

        BigDecimal baseSalary = employee.getPosition() != null && employee.getPosition().getBaseSalary() != null
                ? employee.getPosition().getBaseSalary() : BigDecimal.valueOf(10000000);

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        int standardWorkingDays = countWeekdays(start, end);
        int actualWorkingDays = attendanceRepository.findByEmployeeIdAndWorkDateBetween(employee.getId(), start, end).size();
        if (actualWorkingDays == 0) {
            actualWorkingDays = standardWorkingDays;
        }

        BigDecimal dailySalary = baseSalary.divide(BigDecimal.valueOf(standardWorkingDays), 2, RoundingMode.HALF_UP);
        BigDecimal proratedBase = dailySalary.multiply(BigDecimal.valueOf(actualWorkingDays));
        BigDecimal allowance = baseSalary.multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal bonus = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal hourlyRate = baseSalary.divide(BigDecimal.valueOf(standardWorkingDays * 8), 2, RoundingMode.HALF_UP);
        BigDecimal overtimePay = overtimeHours.multiply(hourlyRate).multiply(new BigDecimal("1.5")).setScale(0, RoundingMode.HALF_UP);

        BigDecimal grossIncome = proratedBase.add(allowance).add(bonus).add(overtimePay).setScale(0, RoundingMode.HALF_UP);

        BigDecimal insuranceBase = baseSalary.min(INSURANCE_SALARY_CAP);
        BigDecimal socialInsurance = insuranceBase.multiply(SOCIAL_INSURANCE_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal healthInsurance = insuranceBase.multiply(HEALTH_INSURANCE_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal unemploymentInsurance = insuranceBase.multiply(UNEMPLOYMENT_INSURANCE_RATE).setScale(0, RoundingMode.HALF_UP);

        BigDecimal taxableIncome = grossIncome
                .subtract(socialInsurance)
                .subtract(healthInsurance)
                .subtract(unemploymentInsurance)
                .subtract(BigDecimal.valueOf(11000000));
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        BigDecimal personalIncomeTax = taxableIncome.multiply(PERSONAL_INCOME_TAX_RATE).setScale(0, RoundingMode.HALF_UP);

        BigDecimal totalDeduction = socialInsurance
                .add(healthInsurance)
                .add(unemploymentInsurance)
                .add(personalIncomeTax);
        BigDecimal netSalary = grossIncome.subtract(totalDeduction).setScale(0, RoundingMode.HALF_UP);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .salaryMonth(month)
                .baseSalary(baseSalary)
                .allowance(allowance)
                .bonus(bonus)
                .deduction(totalDeduction)
                .workingDays(actualWorkingDays)
                .standardWorkingDays(standardWorkingDays)
                .actualWorkingDays(actualWorkingDays)
                .overtimeHours(overtimeHours)
                .overtimePay(overtimePay)
                .socialInsurance(socialInsurance)
                .healthInsurance(healthInsurance)
                .unemploymentInsurance(unemploymentInsurance)
                .personalIncomeTax(personalIncomeTax)
                .grossIncome(grossIncome)
                .totalDeduction(totalDeduction)
                .netSalary(netSalary)
                .status(PayrollStatus.DRAFT)
                .build();

        payrollRepository.save(payroll);
        return mapper.toPayrollResponse(payroll);
    }

    private int countWeekdays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }
}
