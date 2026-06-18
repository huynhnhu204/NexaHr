package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.AttendanceResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.exception.BadRequestException;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.impl.AttendanceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Chấm công — check-in/check-out có ảnh và vị trí")
public class AttendanceController {

    private final AttendanceServiceImpl attendanceService;

    @PostMapping(value = "/check-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Chấm công vào", description = "Bắt buộc ảnh minh chứng và tọa độ GPS trong phạm vi công ty")
    public ApiResponse<AttendanceResponse> checkIn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String note) {
        Long employeeId = requireEmployeeId(userDetails);
        return ApiResponse.success("Chấm công thành công.",
                attendanceService.checkIn(employeeId, photo, latitude, longitude, address, note));
    }

    @PostMapping(value = "/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Chấm công ra", description = "Bắt buộc ảnh minh chứng và tọa độ GPS trong phạm vi công ty")
    public ApiResponse<AttendanceResponse> checkOut(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("photo") MultipartFile photo,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String note) {
        Long employeeId = requireEmployeeId(userDetails);
        return ApiResponse.success("Chấm công thành công.",
                attendanceService.checkOut(employeeId, photo, latitude, longitude, address, note));
    }

    @GetMapping("/today")
    @Operation(summary = "Chấm công hôm nay", description = "Trạng thái chấm công của user hiện tại")
    public ApiResponse<AttendanceResponse> getToday(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long employeeId = attendanceService.resolveEmployeeId(userDetails.getUser().getId());
        return ApiResponse.success(attendanceService.getToday(employeeId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<AttendanceResponse>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attendanceService.getAll(employeeId, startDate, endDate,
                PageRequest.of(page, size, Sort.by("workDate").descending())));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<AttendanceResponse>> getMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long employeeId = attendanceService.resolveEmployeeId(userDetails.getUser().getId());
        return ApiResponse.success(attendanceService.getMyAttendance(employeeId, startDate, endDate,
                PageRequest.of(page, size, Sort.by("workDate").descending())));
    }

    private Long requireEmployeeId(CustomUserDetails userDetails) {
        Long employeeId = attendanceService.resolveEmployeeId(userDetails.getUser().getId());
        if (employeeId == null) {
            throw new BadRequestException("Không tìm thấy hồ sơ nhân viên cho tài khoản này");
        }
        return employeeId;
    }
}
