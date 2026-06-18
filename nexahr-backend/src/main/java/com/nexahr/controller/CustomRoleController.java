package com.nexahr.controller;

import com.nexahr.dto.request.CustomRoleRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.CustomRoleResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.Audited;
import com.nexahr.service.CustomRoleService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-roles")
@RequiredArgsConstructor
@PreAuthorize("@permissionChecker.has('PERMISSIONS_MANAGE')")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    @GetMapping
    public ApiResponse<List<CustomRoleResponse>> list() {
        return ApiResponse.success(customRoleService.list(requireCompanyId()));
    }

    @PostMapping
    @Audited(action = "CREATE", entityType = "CUSTOM_ROLE", details = "Tạo vai trò tùy chỉnh")
    public ApiResponse<CustomRoleResponse> create(@Valid @RequestBody CustomRoleRequest request) {
        return ApiResponse.success(customRoleService.create(requireCompanyId(), request));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", entityType = "CUSTOM_ROLE", details = "Cập nhật vai trò tùy chỉnh")
    public ApiResponse<CustomRoleResponse> update(@PathVariable Long id, @Valid @RequestBody CustomRoleRequest request) {
        return ApiResponse.success(customRoleService.update(requireCompanyId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", entityType = "CUSTOM_ROLE", details = "Xóa vai trò tùy chỉnh")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customRoleService.delete(requireCompanyId(), id);
        return ApiResponse.success("Đã xóa vai trò", null);
    }

    @PutMapping("/assign/{userId}")
    public ApiResponse<Void> assign(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> body) {
        customRoleService.assignToUser(requireCompanyId(), userId, body.get("customRoleId"));
        return ApiResponse.success("Đã gán vai trò", null);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
