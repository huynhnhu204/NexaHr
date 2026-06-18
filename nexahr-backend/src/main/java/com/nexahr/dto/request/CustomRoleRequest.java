package com.nexahr.dto.request;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CustomRoleRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    @NotNull
    private Role baseRole;

    private Boolean active;

    private List<PermissionGrant> permissions;

    @Data
    public static class PermissionGrant {
        private PermissionCode permission;
        private Boolean granted;
    }
}
