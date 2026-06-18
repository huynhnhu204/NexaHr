package com.nexahr.controller;

import com.nexahr.dto.response.AnalyticsResponse;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.AnalyticsService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ApiResponse<AnalyticsResponse> getAnalytics() {
        return ApiResponse.success(analyticsService.getAnalytics(requireCompanyId()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Resource> exportWorkforce() {
        Resource resource = analyticsService.exportWorkforceReport(requireCompanyId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=workforce-analytics.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
