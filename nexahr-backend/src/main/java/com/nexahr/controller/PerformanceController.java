package com.nexahr.controller;

import com.nexahr.dto.request.PerformanceFinalizeRequest;
import com.nexahr.dto.request.PerformanceReviewRequest;
import com.nexahr.dto.request.PerformanceSelfReviewRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.PerformanceReviewResponse;
import com.nexahr.security.Audited;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.impl.PerformanceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceServiceImpl performanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PageResponse<PerformanceReviewResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(performanceService.getAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<PerformanceReviewResponse>> getMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long employeeId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success(performanceService.getMy(employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<PageResponse<PerformanceReviewResponse>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(performanceService.getByEmployee(employeeId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Audited(action = "CREATE", entityType = "PERFORMANCE", details = "Tạo đánh giá hiệu suất")
    public ApiResponse<PerformanceReviewResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PerformanceReviewRequest request) {
        Long reviewerId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success("Review created", performanceService.create(reviewerId, request));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<PerformanceReviewResponse> publish(@PathVariable Long id) {
        return ApiResponse.success("Đã gửi cho nhân viên tự đánh giá", performanceService.publish(id));
    }

    @PutMapping("/{id}/self-review")
    public ApiResponse<PerformanceReviewResponse> submitSelfReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PerformanceSelfReviewRequest request) {
        Long employeeId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success("Đã gửi tự đánh giá",
                performanceService.submitSelfReview(id, employeeId, request));
    }

    @PutMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Audited(action = "APPROVE", entityType = "PERFORMANCE", details = "Hoàn tất đánh giá hiệu suất")
    public ApiResponse<PerformanceReviewResponse> finalizeReview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PerformanceFinalizeRequest request) {
        Long reviewerId = userDetails.getUser().getEmployee().getId();
        return ApiResponse.success("Đã hoàn tất đánh giá",
                performanceService.finalizeReview(id, reviewerId, request));
    }
}
