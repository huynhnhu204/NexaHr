package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.MobileSummaryResponse;
import com.nexahr.security.CustomUserDetails;
import com.nexahr.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;

    @GetMapping("/summary")
    public ApiResponse<MobileSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(mobileService.getSummary(userDetails.getUser().getId()));
    }
}
