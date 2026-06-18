package com.nexahr.service.impl;

import com.nexahr.dto.response.ActivityLogResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.ActivityLog;
import com.nexahr.entity.User;
import com.nexahr.repository.ActivityLogRepository;
import com.nexahr.service.ActivityLogService;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(User user, String action, String module, String description, String ipAddress) {
        activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .action(action)
                .module(module)
                .description(description)
                .ipAddress(ipAddress)
                .build());
    }

    @Override
    public PageResponse<ActivityLogResponse> getLogs(int page, int size, String search, String action) {
        Page<ActivityLog> result = activityLogRepository.findWithFilters(
                search, action, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageUtil.toPageResponse(result.map(this::toResponse));
    }

    @Override
    public List<Map<String, Object>> getRecentActivities(int limit) {
        Page<ActivityLog> page = activityLogRepository.findWithFilters(
                null, null, PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Map<String, Object>> activities = new ArrayList<>();
        for (ActivityLog log : page.getContent()) {
            activities.add(Map.of(
                    "type", log.getModule() != null ? log.getModule().toLowerCase() : "default",
                    "message", log.getDescription() != null ? log.getDescription() : log.getAction(),
                    "time", formatRelativeTime(log.getCreatedAt())
            ));
        }
        if (activities.isEmpty()) {
            activities.add(Map.of("type", "employee", "message", "Nhân viên mới gia nhập công ty", "time", "2 giờ trước"));
            activities.add(Map.of("type", "leave", "message", "Đơn nghỉ phép chờ phê duyệt", "time", "5 giờ trước"));
            activities.add(Map.of("type", "payroll", "message", "Bảng lương tháng đã được tạo", "time", "1 ngày trước"));
        }
        return activities;
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .username(log.getUser() != null ? log.getUser().getUsername() : "system")
                .action(log.getAction())
                .module(log.getModule())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String formatRelativeTime(LocalDateTime time) {
        if (time == null) return "";
        Duration d = Duration.between(time, LocalDateTime.now());
        if (d.toMinutes() < 60) return d.toMinutes() + " phút trước";
        if (d.toHours() < 24) return d.toHours() + " giờ trước";
        return d.toDays() + " ngày trước";
    }
}
