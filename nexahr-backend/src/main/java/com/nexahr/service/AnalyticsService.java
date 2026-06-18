package com.nexahr.service;

import com.nexahr.dto.response.AnalyticsResponse;
import org.springframework.core.io.Resource;

public interface AnalyticsService {
    AnalyticsResponse getAnalytics(Long companyId);
    Resource exportWorkforceReport(Long companyId);
}
