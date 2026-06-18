package com.nexahr.controller;

import com.nexahr.dto.request.*;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.ForgotPasswordResponse;
import com.nexahr.dto.response.GoogleAuthConfigResponse;
import com.nexahr.dto.response.SamlSsoResponse;
import com.nexahr.dto.response.UserResponse;
import com.nexahr.security.Audited;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.dto.response.SamlSsoResponse;
import com.nexahr.service.AuthService;
import com.nexahr.service.AuditLogService;
import com.nexahr.service.SamlService;
import com.nexahr.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final SamlService samlService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);
        auditLogService.log(
                null,
                "LOGIN",
                "AUTH",
                response.getUserId(),
                "Đăng nhập: " + request.getEmail(),
                RequestContextUtil.clientIp(httpRequest),
                RequestContextUtil.userAgent(httpRequest),
                null
        );
        return ApiResponse.success("Đăng nhập thành công", response);
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Đăng ký thành công", authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.getCurrentUser(userDetails.getUsername()));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    @PostMapping("/change-password")
    @Audited(action = "UPDATE", entityType = "AUTH", details = "Đổi mật khẩu")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.success(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Đặt lại mật khẩu thành công", null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ApiResponse.success("Đăng xuất thành công", null);
    }

    @GetMapping("/google/config")
    public ApiResponse<GoogleAuthConfigResponse> getGoogleConfig() {
        return ApiResponse.success(authService.getGoogleAuthConfig());
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return ApiResponse.success("Đăng nhập Google thành công", authService.loginWithGoogle(request));
    }

    @GetMapping("/saml/sso/{companyCode}")
    public ApiResponse<SamlSsoResponse> getSamlSso(@PathVariable String companyCode) {
        return ApiResponse.success(samlService.getSsoInit(companyCode));
    }
}
