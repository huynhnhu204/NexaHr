package com.nexahr.service;

import com.nexahr.dto.request.ScheduledReportRequest;
import com.nexahr.dto.response.ScheduledReportResponse;

import java.util.List;

public interface ScheduledReportService {
    List<ScheduledReportResponse> list(Long companyId);
    ScheduledReportResponse create(Long companyId, ScheduledReportRequest request);
    void delete(Long companyId, Long id);
    void runDueReports();
}
