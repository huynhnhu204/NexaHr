package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.CompanyAttendanceLocationResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.service.impl.AttendanceServiceImpl;
import com.nexahr.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/current")
@RequiredArgsConstructor
@Tag(name = "Company Location", description = "Vị trí chấm công của công ty hiện tại")
public class CompanyLocationController {

    private final AttendanceServiceImpl attendanceService;

    @GetMapping("/attendance-location")
    @Operation(summary = "Lấy vị trí chấm công công ty", description = "Tọa độ và bán kính cho phép của công ty đang chọn")
    public ApiResponse<CompanyAttendanceLocationResponse> getAttendanceLocation() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return ApiResponse.success(attendanceService.getCompanyAttendanceLocation(companyId));
    }
}
