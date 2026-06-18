package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSummaryResponse {
    private long totalEmployees;
    private long newEmployeesThisMonth;
    private long pendingLeaveRequests;
    private BigDecimal totalPayrollThisMonth;
    private long activeRecruitment;
    private long openPositions;
    private List<ActivityItem> recentActivities;

    @Data
    @Builder
    public static class ActivityItem {
        private String type;
        private String message;
        private String time;
    }
}
