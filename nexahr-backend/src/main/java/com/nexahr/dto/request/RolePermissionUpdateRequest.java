package com.nexahr.dto.request;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolePermissionUpdateRequest {
    @NotNull
    private Role role;

    @NotNull
    private PermissionCode permission;

    @NotNull
    private Boolean granted;
}
