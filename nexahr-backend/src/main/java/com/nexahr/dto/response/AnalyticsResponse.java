package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private AnalyticsOverviewResponse overview;
    private List<Map<String, Object>> headcountTrend;
    private List<Map<String, Object>> leaveByType;
    private List<Map<String, Object>> recruitmentFunnel;
    private List<Map<String, Object>> attendanceOverview;
    private List<Map<String, Object>> payrollTrend;
    private List<Map<String, Object>> departmentHeadcount;
}
