package com.nexahr.service;

import com.nexahr.dto.response.ActivityLogResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.User;

import java.util.List;
import java.util.Map;

public interface ActivityLogService {
    void log(User user, String action, String module, String description, String ipAddress);
    PageResponse<ActivityLogResponse> getLogs(int page, int size, String search, String action);
    List<Map<String, Object>> getRecentActivities(int limit);
}
