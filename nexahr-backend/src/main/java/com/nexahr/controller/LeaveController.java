package com.nexahr.controller;

import com.nexahr.dto.request.LeaveRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.LeaveResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.enums.LeaveStatus;
import com.nexahr.security.Audited;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.impl.LeaveServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveServiceImpl leaveService;

    @PostMapping
    public ApiResponse<LeaveResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LeaveRequest request) {
        Long employeeId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success("Leave request submitted", leaveService.create(employeeId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<LeaveResponse>> getAll(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(leaveService.getAll(status, employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<LeaveResponse>> getMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long employeeId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success(leaveService.getMy(employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("@permissionChecker.has('LEAVE_APPROVE')")
    @Audited(action = "APPROVE", entityType = "LEAVE", details = "Duyệt đơn nghỉ phép")
    public ApiResponse<LeaveResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("Leave approved", leaveService.approve(id, userDetails.getUser()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("@permissionChecker.has('LEAVE_APPROVE')")
    @Audited(action = "REJECT", entityType = "LEAVE", details = "Từ chối đơn nghỉ phép")
    public ApiResponse<LeaveResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        return ApiResponse.success("Leave rejected",
                leaveService.reject(id, userDetails.getUser(), body.getOrDefault("reason", "No reason provided")));
    }
}
