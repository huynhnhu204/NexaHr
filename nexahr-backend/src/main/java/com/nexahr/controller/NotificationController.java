package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.NotificationResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(notificationService.getByUser(userDetails.getUser().getId(),
                PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUser().getId());
        return ApiResponse.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ApiResponse.success(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser().getId());
        return ApiResponse.success("All marked as read", null);
    }
}
