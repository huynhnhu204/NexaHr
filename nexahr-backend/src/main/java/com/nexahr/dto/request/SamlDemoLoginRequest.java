package com.nexahr.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SamlDemoLoginRequest {
    @NotBlank
    private String companyCode;

    @NotBlank
    @Email
    private String email;
}
