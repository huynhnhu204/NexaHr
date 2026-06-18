package com.nexahr.config;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class DefaultRolePermissions {

    private static final Map<Role, Set<PermissionCode>> DEFAULTS = new EnumMap<>(Role.class);

    static {
        DEFAULTS.put(Role.ADMIN, EnumSet.allOf(PermissionCode.class));

        DEFAULTS.put(Role.HR, EnumSet.of(
                PermissionCode.EMPLOYEE_VIEW,
                PermissionCode.EMPLOYEE_MANAGE,
                PermissionCode.DEPARTMENT_MANAGE,
                PermissionCode.PAYROLL_VIEW_ALL,
                PermissionCode.PAYROLL_MANAGE,
                PermissionCode.LEAVE_APPROVE,
                PermissionCode.LEAVE_VIEW_ALL,
                PermissionCode.RECRUITMENT_MANAGE,
                PermissionCode.ANALYTICS_VIEW
        ));

        DEFAULTS.put(Role.MANAGER, EnumSet.of(
                PermissionCode.EMPLOYEE_VIEW,
                PermissionCode.LEAVE_APPROVE,
                PermissionCode.LEAVE_VIEW_ALL,
                PermissionCode.ANALYTICS_VIEW
        ));

        DEFAULTS.put(Role.EMPLOYEE, EnumSet.noneOf(PermissionCode.class));
    }

    private DefaultRolePermissions() {}

    public static Set<PermissionCode> forRole(Role role) {
        return DEFAULTS.getOrDefault(role, EnumSet.noneOf(PermissionCode.class));
    }

    public static boolean isGrantedByDefault(Role role, PermissionCode permission) {
        return forRole(role).contains(permission);
    }
}
