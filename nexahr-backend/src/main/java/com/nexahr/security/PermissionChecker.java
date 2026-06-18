package com.nexahr.security;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import com.nexahr.service.PermissionService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("permissionChecker")
@RequiredArgsConstructor
public class PermissionChecker {

    private final PermissionService permissionService;

    public boolean has(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }
        Role role = userDetails.getUser().getRole();
        Long companyId = TenantContext.getCompanyId();
        Long userId = userDetails.getUser().getId();
        try {
            PermissionCode code = PermissionCode.valueOf(permissionCode);
            return permissionService.hasPermission(userId, role, companyId, code);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
