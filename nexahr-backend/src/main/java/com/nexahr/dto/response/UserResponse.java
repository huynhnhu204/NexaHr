package com.nexahr.dto.response;

import com.nexahr.entity.enums.Role;
import com.nexahr.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private String fullName;
    private Long employeeId;
    private String avatar;
}
