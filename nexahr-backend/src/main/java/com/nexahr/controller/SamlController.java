package com.nexahr.controller;

import com.nexahr.dto.request.SamlConfigRequest;
import com.nexahr.dto.request.SamlDemoLoginRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.SamlConfigResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.SamlService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SamlController {

    private final SamlService samlService;

    @GetMapping("/api/integrations/saml")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SamlConfigResponse> getConfig() {
        return ApiResponse.success(samlService.getConfig(requireCompanyId()));
    }

    @PutMapping("/api/integrations/saml")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SamlConfigResponse> updateConfig(@Valid @RequestBody SamlConfigRequest request) {
        return ApiResponse.success("Cập nhật SAML thành công",
                samlService.updateConfig(requireCompanyId(), request));
    }

    @GetMapping(value = "/api/public/saml/{companyCode}/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public String getMetadata(@PathVariable String companyCode) {
        return samlService.getMetadata(companyCode);
    }

    @PostMapping("/api/auth/saml/demo")
    public ApiResponse<AuthResponse> demoLogin(@Valid @RequestBody SamlDemoLoginRequest request) {
        return ApiResponse.success("Đăng nhập SAML demo thành công", samlService.demoLogin(request));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
