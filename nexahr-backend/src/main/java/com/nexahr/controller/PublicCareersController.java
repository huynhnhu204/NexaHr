package com.nexahr.controller;

import com.nexahr.dto.request.PublicApplyRequest;
import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PublicJobDetailResponse;
import com.nexahr.dto.response.PublicJobResponse;
import com.nexahr.service.PublicCareersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/careers/{companyCode}/jobs")
@RequiredArgsConstructor
public class PublicCareersController {

    private final PublicCareersService publicCareersService;

    @GetMapping
    public ApiResponse<List<PublicJobResponse>> listJobs(@PathVariable String companyCode) {
        return ApiResponse.success(publicCareersService.listJobs(companyCode));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<PublicJobDetailResponse> getJob(
            @PathVariable String companyCode,
            @PathVariable Long jobId) {
        return ApiResponse.success(publicCareersService.getJob(companyCode, jobId));
    }

    @PostMapping("/{jobId}/apply")
    public ApiResponse<Void> apply(
            @PathVariable String companyCode,
            @PathVariable Long jobId,
            @Valid @RequestBody PublicApplyRequest request) {
        publicCareersService.apply(companyCode, jobId, request);
        return ApiResponse.success("Ứng tuyển thành công", null);
    }
}
