package com.nexahr.service.impl;

import com.nexahr.dto.request.*;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.ForgotPasswordResponse;
import com.nexahr.dto.response.GoogleAuthConfigResponse;
import com.nexahr.dto.response.UserResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.AuthProvider;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.Role;
import com.nexahr.entity.enums.UserStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.*;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.security.JwtService;
import com.nexahr.service.ActivityLogService;
import com.nexahr.service.AuthService;
import com.nexahr.service.EmailService;
import com.nexahr.util.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AuthResponse response = buildAuthResponse(userDetails);
        activityLogService.log(userDetails.getUser(), "LOGIN", "AUTH", "Đăng nhập hệ thống", null);
        return response;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.EMPLOYEE;
        if (role == Role.ADMIN) {
            role = Role.EMPLOYEE;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Company defaultCompany = companyRepository.findByCode("NEXA-DEMO")
                .orElseThrow(() -> new BadRequestException("Chưa có công ty mặc định trong hệ thống"));

        membershipRepository.save(CompanyMembership.builder()
                .user(user)
                .company(defaultCompany)
                .isDefault(true)
                .build());

        String employeeCode = "EMP" + String.format("%04d", user.getId());
        Employee employee = Employee.builder()
                .user(user)
                .company(defaultCompany)
                .employeeCode(employeeCode)
                .fullName(request.getFullName())
                .hireDate(LocalDate.now())
                .employmentStatus(EmploymentStatus.PROBATION)
                .annualLeaveBalance(12)
                .build();
        employeeRepository.save(employee);
        user.setEmployee(employee);

        return buildAuthResponse(new CustomUserDetails(user));
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng"));
        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(employee != null ? employee.getFullName() : user.getUsername())
                .employeeId(employee != null ? employee.getId() : null)
                .avatar(employee != null ? employee.getAvatar() : null)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token không hợp lệ"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token đã hết hạn");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(new CustomUserDetails(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        activityLogService.log(user, "UPDATE", "AUTH", "Đổi mật khẩu", null);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email không tồn tại trong hệ thống"));

        passwordResetTokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build());

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        ForgotPasswordResponse.ForgotPasswordResponseBuilder builder = ForgotPasswordResponse.builder()
                .message("Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi");

        if (!mailEnabled) {
            builder.message("Liên kết đặt lại mật khẩu (dev: dùng token bên dưới)")
                    .resetToken(token);
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token không hợp lệ"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token đã hết hạn hoặc đã sử dụng");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.deleteByUserId(user.getId());
        activityLogService.log(user, "UPDATE", "AUTH", "Đặt lại mật khẩu", null);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) return;
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            activityLogService.log(rt.getUser(), "LOGOUT", "AUTH", "Đăng xuất hệ thống", null);
        });
    }

    @Override
    @Transactional
    public AuthResponse issueAuthResponse(User user, Long companyId) {
        return buildAuthResponse(new CustomUserDetails(user), companyId);
    }

    @Override
    public GoogleAuthConfigResponse getGoogleAuthConfig() {
        return GoogleAuthConfigResponse.builder()
                .enabled(googleTokenVerifier.isEnabled())
                .clientId(googleTokenVerifier.getClientId())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByGoogleId(googleUser.googleId())
                .or(() -> userRepository.findByEmail(googleUser.email()))
                .orElseThrow(() -> new BadRequestException(
                        "Email chưa được đăng ký trong hệ thống. Liên hệ quản trị viên."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Tài khoản đã bị vô hiệu hóa");
        }

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleUser.googleId());
            user.setAuthProvider(AuthProvider.GOOGLE);
            userRepository.save(user);
        }

        AuthResponse response = buildAuthResponse(new CustomUserDetails(user));
        activityLogService.log(user, "LOGIN", "AUTH", "Đăng nhập qua Google", null);
        return response;
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails) {
        CompanyMembership membership = membershipRepository.findByUserIdAndIsDefaultTrue(userDetails.getUser().getId())
                .or(() -> membershipRepository.findByUserId(userDetails.getUser().getId()).stream().findFirst())
                .orElse(null);

        Long companyId = membership != null ? membership.getCompany().getId() : null;
        return buildAuthResponse(userDetails, companyId);
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails, Long companyId) {
        User user = userDetails.getUser();
        Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        Company company = null;

        if (companyId != null) {
            company = companyRepository.findById(companyId).orElse(null);
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        if (companyId != null) {
            extraClaims.put("companyId", companyId);
        }

        String accessToken = jwtService.generateToken(extraClaims, userDetails);
        String refreshTokenStr = jwtService.generateRefreshToken(userDetails);

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpiration() / 1000))
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .fullName(employee != null ? employee.getFullName() : user.getUsername())
                .employeeId(employee != null ? employee.getId() : null)
                .companyId(companyId)
                .companyName(company != null ? company.getName() : null)
                .build();
    }
}
