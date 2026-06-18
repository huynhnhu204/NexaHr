package com.nexahr.controller;

import com.nexahr.dto.response.ApiResponse;
import com.nexahr.dto.response.PublicCompanyResponse;
import com.nexahr.service.PublicCareersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/careers/{companyCode}")
@RequiredArgsConstructor
public class PublicCompanyController {

    private final PublicCareersService publicCareersService;

    @GetMapping
    public ApiResponse<PublicCompanyResponse> getCompanyInfo(@PathVariable String companyCode) {
        return ApiResponse.success(publicCareersService.getCompanyInfo(companyCode));
    }
}
