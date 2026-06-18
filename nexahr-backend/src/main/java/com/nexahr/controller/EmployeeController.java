package com.nexahr.controller;

import com.nexahr.dto.request.EmployeeRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.EmployeeResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.dto.response.EmployeeTimelineEventResponse;
import com.nexahr.service.EmployeeDocumentService;
import com.nexahr.service.impl.EmployeeServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeServiceImpl employeeService;
    private final EmployeeDocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<EmployeeResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(employeeService.getAll(search, departmentId, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<List<EmployeeTimelineEventResponse>> getTimeline(@PathVariable Long id) {
        return ApiResponse.success(documentService.getTimeline(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.success("Employee created", employeeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.success("Employee updated", employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.success("Employee deactivated", null);
    }
}
