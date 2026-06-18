package com.nexahr.service;

import com.nexahr.dto.request.PublicApplyRequest;
import com.nexahr.dto.response.PublicCompanyResponse;
import com.nexahr.dto.response.PublicJobDetailResponse;
import com.nexahr.dto.response.PublicJobResponse;

import java.util.List;

public interface PublicCareersService {
    PublicCompanyResponse getCompanyInfo(String companyCode);
    List<PublicJobResponse> listJobs(String companyCode);
    PublicJobDetailResponse getJob(String companyCode, Long jobId);
    void apply(String companyCode, Long jobId, PublicApplyRequest request);
}
