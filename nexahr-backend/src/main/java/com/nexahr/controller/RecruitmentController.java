package com.nexahr.controller;

import com.nexahr.dto.request.CandidateRequest;
import com.nexahr.dto.request.JobPostingRequest;
import com.nexahr.dto.response.*;
import com.nexahr.entity.enums.CandidateStatus;
import com.nexahr.service.impl.RecruitmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentServiceImpl recruitmentService;

    @GetMapping("/api/jobs")
    public ApiResponse<PageResponse<JobPostingResponse>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(recruitmentService.getJobs(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/api/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<JobPostingResponse> createJob(@Valid @RequestBody JobPostingRequest request) {
        return ApiResponse.success("Job created", recruitmentService.createJob(request));
    }

    @PutMapping("/api/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<JobPostingResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobPostingRequest request) {
        return ApiResponse.success("Job updated", recruitmentService.updateJob(id, request));
    }

    @DeleteMapping("/api/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteJob(@PathVariable Long id) {
        recruitmentService.deleteJob(id);
        return ApiResponse.success("Job deleted", null);
    }

    @GetMapping("/api/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PageResponse<CandidateResponse>> getCandidates(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) CandidateStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(recruitmentService.getCandidates(jobId, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/api/candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<CandidateResponse> createCandidate(@Valid @RequestBody CandidateRequest request) {
        return ApiResponse.success("Candidate added", recruitmentService.createCandidate(request));
    }

    @PutMapping("/api/candidates/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<CandidateResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        CandidateStatus status = CandidateStatus.valueOf(body.get("status"));
        return ApiResponse.success("Status updated", recruitmentService.updateCandidateStatus(id, status));
    }
}
