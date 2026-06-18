package com.nexahr.controller;

import com.nexahr.dto.request.InterviewRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.InterviewResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.enums.InterviewStatus;
import com.nexahr.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PageResponse<InterviewResponse>> getAll(
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long interviewerId,
            @RequestParam(required = false) InterviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(interviewService.getAll(candidateId, interviewerId, status,
                PageRequest.of(page, size, Sort.by("scheduledAt").descending())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<InterviewResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(interviewService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<InterviewResponse> create(@Valid @RequestBody InterviewRequest request) {
        return ApiResponse.success("Tạo lịch phỏng vấn thành công", interviewService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<InterviewResponse> update(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        return ApiResponse.success("Cập nhật lịch phỏng vấn thành công", interviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ApiResponse.success("Xóa lịch phỏng vấn thành công", null);
    }
}
