package com.nexahr.dto.response;

import com.nexahr.entity.enums.PermissionCode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserPermissionsResponse {
    private List<PermissionCode> permissions;
}
