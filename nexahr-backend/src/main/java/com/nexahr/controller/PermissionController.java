package com.nexahr.controller;

import com.nexahr.dto.request.RolePermissionUpdateRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PermissionMatrixResponse;
import com.nexahr.dto.response.UserPermissionsResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.Audited;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.PermissionService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/me")
    public ApiResponse<UserPermissionsResponse> myPermissions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(permissionService.getCurrentUserPermissions(
                userDetails.getUser().getId(), userDetails.getUser().getRole(), requireCompanyId()));
    }

    @GetMapping("/matrix")
    @PreAuthorize("@permissionChecker.has('PERMISSIONS_MANAGE')")
    public ApiResponse<PermissionMatrixResponse> getMatrix() {
        return ApiResponse.success(permissionService.getMatrix(requireCompanyId()));
    }

    @PutMapping("/matrix")
    @PreAuthorize("@permissionChecker.has('PERMISSIONS_MANAGE')")
    @Audited(action = "UPDATE", entityType = "PERMISSION", details = "Cập nhật ma trận phân quyền")
    public ApiResponse<PermissionMatrixResponse> updateMatrix(
            @Valid @RequestBody List<RolePermissionUpdateRequest> updates) {
        return ApiResponse.success(permissionService.updatePermissions(requireCompanyId(), updates));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
