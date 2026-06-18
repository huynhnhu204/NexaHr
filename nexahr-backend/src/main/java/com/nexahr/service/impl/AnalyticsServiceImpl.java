package com.nexahr.service.impl;

import com.nexahr.dto.response.AnalyticsOverviewResponse;
import com.nexahr.dto.response.AnalyticsResponse;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.JobStatus;
import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.*;
import com.nexahr.service.AnalyticsService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public AnalyticsResponse getAnalytics(Long companyId) {
        long active = employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.ACTIVE, companyId)
                + employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.PROBATION, companyId);
        long resigned = employeeRepository.countResignedByCompanyId(companyId);
        long totalWithResigned = active + resigned;
        double turnover = totalWithResigned == 0 ? 0.0
                : BigDecimal.valueOf(resigned * 100.0 / totalWithResigned).setScale(1, RoundingMode.HALF_UP).doubleValue();

        LocalDate monthStart = YearMonth.now().atDay(1);
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        AnalyticsOverviewResponse overview = AnalyticsOverviewResponse.builder()
                .totalEmployees(active)
                .newHiresThisMonth(employeeRepository.countNewEmployeesSince(companyId, monthStart))
                .resignedThisYear(resigned)
                .turnoverRate(turnover)
                .pendingLeaves(leaveRepository.countByStatusAndCompanyId(LeaveStatus.PENDING, companyId))
                .approvedLeavesThisMonth(leaveRepository.countApprovedSince(companyId, monthStart.atStartOfDay()))
                .payrollCostThisMonth(payrollRepository.sumNetSalaryByMonthAndCompanyId(currentMonth, companyId))
                .openPositions(jobPostingRepository.countByStatusAndCompanyId(JobStatus.OPEN, companyId))
                .totalCandidates(candidateRepository.countByCompanyId(companyId))
                .build();

        return AnalyticsResponse.builder()
                .overview(overview)
                .headcountTrend(buildHeadcountTrend(companyId))
                .leaveByType(mapLeaveTypes(leaveRepository.countByLeaveTypeAndCompanyId(companyId)))
                .recruitmentFunnel(mapRecruitmentFunnel(candidateRepository.countByStatusAndCompanyId(companyId)))
                .attendanceOverview(mapAttendance(attendanceRepository.countByStatusAndCompanySince(companyId, monthStart)))
                .payrollTrend(mapPayroll(payrollRepository.sumSalaryByMonthAndCompanyId(companyId)))
                .departmentHeadcount(mapDepartments(departmentRepository.countEmployeesByDepartment(companyId)))
                .build();
    }

    @Override
    public Resource exportWorkforceReport(Long companyId) {
        AnalyticsResponse analytics = getAnalytics(companyId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Workforce Analytics");
            int rowIdx = 0;

            Row title = sheet.createRow(rowIdx++);
            title.createCell(0).setCellValue("NexaHR - Báo cáo phân tích nhân sự");

            AnalyticsOverviewResponse o = analytics.getOverview();
            String[][] metrics = {
                    {"Tổng nhân viên", String.valueOf(o.getTotalEmployees())},
                    {"Tuyển mới tháng này", String.valueOf(o.getNewHiresThisMonth())},
                    {"Đã nghỉ việc", String.valueOf(o.getResignedThisYear())},
                    {"Tỷ lệ turnover (%)", String.valueOf(o.getTurnoverRate())},
                    {"Nghỉ phép chờ duyệt", String.valueOf(o.getPendingLeaves())},
                    {"Chi phí lương tháng", o.getPayrollCostThisMonth() != null ? o.getPayrollCostThisMonth().toPlainString() : "0"},
            };
            for (String[] m : metrics) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m[0]);
                row.createCell(1).setCellValue(m[1]);
            }

            rowIdx++;
            Row deptHeader = sheet.createRow(rowIdx++);
            deptHeader.createCell(0).setCellValue("Phòng ban");
            deptHeader.createCell(1).setCellValue("Số nhân viên");
            for (Map<String, Object> d : analytics.getDepartmentHeadcount()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(d.get("name")));
                row.createCell(1).setCellValue(((Number) d.get("value")).doubleValue());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            throw new BadRequestException("Không thể xuất báo cáo: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> buildHeadcountTrend(Long companyId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            long hires = employeeRepository.countHiredBetween(companyId, ym.atDay(1), ym.atEndOfMonth());
            trend.add(Map.of("month", ym.format(fmt), "hires", hires));
        }
        return trend;
    }

    private List<Map<String, Object>> mapLeaveTypes(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("type", row[0].toString(), "count", row[1]));
        }
        return result;
    }

    private List<Map<String, Object>> mapRecruitmentFunnel(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("status", row[0].toString(), "count", row[1]));
        }
        return result;
    }

    private List<Map<String, Object>> mapAttendance(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("status", row[0].toString(), "count", row[1]));
        }
        if (result.isEmpty()) {
            result.add(Map.of("status", "ON_TIME", "count", 0));
        }
        return result;
    }

    private List<Map<String, Object>> mapPayroll(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("month", row[0], "amount", row[1]));
        }
        return result;
    }

    private List<Map<String, Object>> mapDepartments(List<Object[]> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            result.add(Map.of("name", row[0], "value", row[1]));
        }
        return result;
    }

    public Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
