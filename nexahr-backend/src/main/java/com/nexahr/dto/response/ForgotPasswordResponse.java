package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordResponse {
    private String message;
    private String resetToken;
}
