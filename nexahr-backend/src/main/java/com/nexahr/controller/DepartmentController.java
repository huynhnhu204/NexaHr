package com.nexahr.controller;

import com.nexahr.dto.request.DepartmentRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.DepartmentResponse;
import com.nexahr.service.impl.DepartmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentServiceImpl departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> getAll() {
        return ApiResponse.success(departmentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success("Department created", departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success("Department updated", departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.success("Department deleted", null);
    }
}
