package com.nexahr.service;

import com.nexahr.dto.request.*;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.ForgotPasswordResponse;
import com.nexahr.dto.response.GoogleAuthConfigResponse;
import com.nexahr.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    UserResponse getCurrentUser(String email);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void logout(String refreshToken);
    AuthResponse issueAuthResponse(com.nexahr.entity.User user, Long companyId);
    AuthResponse loginWithGoogle(GoogleAuthRequest request);
    GoogleAuthConfigResponse getGoogleAuthConfig();
}
