package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.DepartmentResponse;
import com.nexahr.dto.response.EmployeeResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.DepartmentRepository;
import com.nexahr.security.ApiKeyPrincipal;
import com.nexahr.service.impl.EmployeeServiceImpl;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('API')")
public class IntegrationApiController {

    private final EmployeeServiceImpl employeeService;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/employees")
    public ApiResponse<PageResponse<EmployeeResponse>> listEmployees(
            @AuthenticationPrincipal ApiKeyPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireScope(principal, "employees:read");
        return ApiResponse.success(employeeService.getAll(null, null, null, PageRequest.of(page, size)));
    }

    @GetMapping("/employees/{id}")
    public ApiResponse<EmployeeResponse> getEmployee(
            @AuthenticationPrincipal ApiKeyPrincipal principal,
            @PathVariable Long id) {
        requireScope(principal, "employees:read");
        return ApiResponse.success(employeeService.getById(id));
    }

    @GetMapping("/departments")
    public ApiResponse<List<DepartmentResponse>> listDepartments(
            @AuthenticationPrincipal ApiKeyPrincipal principal) {
        requireScope(principal, "departments:read");
        Long companyId = TenantContext.getCompanyId();
        List<DepartmentResponse> departments = departmentRepository.findByCompanyId(companyId).stream()
                .map(d -> DepartmentResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .description(d.getDescription())
                        .build())
                .toList();
        return ApiResponse.success(departments);
    }

    private void requireScope(ApiKeyPrincipal principal, String scope) {
        if (principal == null || !principal.hasScope(scope)) {
            throw new BadRequestException("API key không có quyền: " + scope);
        }
    }
}
