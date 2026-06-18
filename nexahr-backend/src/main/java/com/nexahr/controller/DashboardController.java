package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.DashboardSummaryResponse;
import com.nexahr.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/employee-chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<List<Map<String, Object>>> getEmployeeChart() {
        return ApiResponse.success(dashboardService.getEmployeeChart());
    }

    @GetMapping("/payroll-chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<List<Map<String, Object>>> getPayrollChart() {
        return ApiResponse.success(dashboardService.getPayrollChart());
    }

    @GetMapping("/recruitment-chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<List<Map<String, Object>>> getRecruitmentChart() {
        return ApiResponse.success(dashboardService.getRecruitmentChart());
    }

    @GetMapping("/leave-chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<List<Map<String, Object>>> getLeaveChart() {
        return ApiResponse.success(dashboardService.getLeaveChart());
    }
}
