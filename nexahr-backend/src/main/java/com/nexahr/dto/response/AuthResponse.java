package com.nexahr.dto.response;

import com.nexahr.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String username;
    private Role role;
    private String fullName;
    private Long employeeId;
    private Long companyId;
    private String companyName;
}
