package com.nexahr.controller;

import com.nexahr.dto.request.ScheduledReportRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.ScheduledReportResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.ScheduledReportService;
import com.nexahr.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScheduledReportController {

    private final ScheduledReportService scheduledReportService;

    @GetMapping
    public ApiResponse<List<ScheduledReportResponse>> list() {
        return ApiResponse.success(scheduledReportService.list(requireCompanyId()));
    }

    @PostMapping
    public ApiResponse<ScheduledReportResponse> create(@Valid @RequestBody ScheduledReportRequest request) {
        return ApiResponse.success("Tạo lịch báo cáo thành công",
                scheduledReportService.create(requireCompanyId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        scheduledReportService.delete(requireCompanyId(), id);
        return ApiResponse.success("Đã xóa lịch báo cáo", null);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return companyId;
    }
}
