package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.SearchResultResponse;
import com.nexahr.dto.response.ActivityLogResponse;
import com.nexahr.service.ActivityLogService;
import com.nexahr.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ActivityLogService activityLogService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SearchResultResponse>> search(@RequestParam String q) {
        return ApiResponse.success(searchService.globalSearch(q));
    }

    @GetMapping("/activity-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ActivityLogResponse>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action) {
        return ApiResponse.success(activityLogService.getLogs(page, size, search, action));
    }

    @GetMapping("/dashboard/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(activityLogService.getRecentActivities(limit));
    }
}
