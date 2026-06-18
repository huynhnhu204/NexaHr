package com.nexahr.dto.response;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomRoleResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Role baseRole;
    private boolean active;
    private List<PermissionGrant> permissions;
    private int assignedUsers;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class PermissionGrant {
        private PermissionCode permission;
        private boolean granted;
    }
}
