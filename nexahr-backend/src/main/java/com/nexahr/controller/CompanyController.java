package com.nexahr.controller;

import com.nexahr.dto.request.CompanySettingsRequest;
import com.nexahr.dto.request.SwitchCompanyRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.CompanyAttendanceLocationResponse;
import com.nexahr.dto.response.CompanyResponse;
import com.nexahr.dto.response.CompanySettingsResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.CompanyService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CompanyResponse>> getMyCompanies(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(companyService.getMyCompanies(userDetails.getUser().getId()));
    }

    @PostMapping("/switch")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AuthResponse> switchCompany(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SwitchCompanyRequest request) {
        return ApiResponse.success("Chuyển công ty thành công",
                companyService.switchCompany(userDetails.getUser().getId(), request.getCompanyId()));
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanySettingsResponse> getSettings() {
        return ApiResponse.success(companyService.getCompanySettings(requireCompanyId()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanySettingsResponse> updateSettings(
            @Valid @RequestBody CompanySettingsRequest request) {
        return ApiResponse.success("Cập nhật cài đặt công ty thành công",
                companyService.updateCompanySettings(requireCompanyId(), request));
    }

    @GetMapping("/attendance-location")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Vị trí chấm công công ty hiện tại")
    public ApiResponse<CompanyAttendanceLocationResponse> getAttendanceLocation() {
        return ApiResponse.success(companyService.getAttendanceLocation(requireCompanyId()));
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
