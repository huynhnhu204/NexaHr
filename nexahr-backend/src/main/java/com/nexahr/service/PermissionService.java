package com.nexahr.service;

import com.nexahr.dto.request.RolePermissionUpdateRequest;
import com.nexahr.dto.response.PermissionMatrixResponse;
import com.nexahr.dto.response.UserPermissionsResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;

import java.util.List;

public interface PermissionService {
    void seedDefaults(Company company);
    UserPermissionsResponse getCurrentUserPermissions(Long userId, Role role, Long companyId);
    PermissionMatrixResponse getMatrix(Long companyId);
    PermissionMatrixResponse updatePermissions(Long companyId, List<RolePermissionUpdateRequest> updates);
    boolean hasPermission(Long userId, Role role, Long companyId, PermissionCode permission);
}
