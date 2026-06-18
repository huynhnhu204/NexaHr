package com.nexahr.dto.response;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PermissionMatrixResponse {
    private List<PermissionItem> permissions;
    private Map<Role, List<PermissionGrant>> matrix;

    @Data
    @Builder
    public static class PermissionItem {
        private PermissionCode code;
        private String label;
    }

    @Data
    @Builder
    public static class PermissionGrant {
        private PermissionCode permission;
        private boolean granted;
    }
}
