package com.nexahr.controller;

import com.nexahr.dto.request.PushRegisterRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PushConfigResponse;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final PushNotificationService pushNotificationService;

    @GetMapping("/config")
    public ApiResponse<PushConfigResponse> getConfig() {
        return ApiResponse.success(pushNotificationService.getConfig());
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PushRegisterRequest request) {
        pushNotificationService.registerDevice(userDetails.getUser().getId(), request);
        return ApiResponse.success("Đăng ký push thành công", null);
    }

    @DeleteMapping("/unregister")
    public ApiResponse<Void> unregister(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String deviceToken) {
        pushNotificationService.unregisterDevice(userDetails.getUser().getId(), deviceToken);
        return ApiResponse.success("Đã hủy đăng ký push", null);
    }
}
