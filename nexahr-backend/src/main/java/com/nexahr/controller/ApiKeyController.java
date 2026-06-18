package com.nexahr.controller;

import com.nexahr.dto.request.CreateApiKeyRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.ApiKeyResponse;
import com.nexahr.dto.response.CreateApiKeyResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.ApiKeyService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations/api-keys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public ApiResponse<List<ApiKeyResponse>> list() {
        return ApiResponse.success(apiKeyService.listKeys(requireCompanyId()));
    }

    @PostMapping
    public ApiResponse<CreateApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
        return ApiResponse.success("Tạo API key thành công",
                apiKeyService.createKey(requireCompanyId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> revoke(@PathVariable Long id) {
        apiKeyService.revokeKey(requireCompanyId(), id);
        return ApiResponse.success("Đã thu hồi API key", null);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
