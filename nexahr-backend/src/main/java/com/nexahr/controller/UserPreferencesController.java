package com.nexahr.controller;

import com.nexahr.dto.request.NotificationPreferencesRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.NotificationPreferencesResponse;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/preferences")
@RequiredArgsConstructor
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    @GetMapping("/notifications")
    public ApiResponse<NotificationPreferencesResponse> getNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userPreferencesService.getPreferences(userDetails.getUser().getId()));
    }

    @PutMapping("/notifications")
    public ApiResponse<NotificationPreferencesResponse> updateNotificationPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotificationPreferencesRequest request) {
        return ApiResponse.success("Cập nhật tùy chọn thông báo thành công",
                userPreferencesService.updatePreferences(userDetails.getUser().getId(), request));
    }
}
