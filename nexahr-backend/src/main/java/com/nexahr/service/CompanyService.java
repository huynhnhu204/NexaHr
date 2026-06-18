package com.nexahr.service;

import com.nexahr.dto.request.CompanySettingsRequest;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.CompanyAttendanceLocationResponse;
import com.nexahr.dto.response.CompanyResponse;
import com.nexahr.dto.response.CompanySettingsResponse;

import java.util.List;

public interface CompanyService {
    List<CompanyResponse> getMyCompanies(Long userId);
    AuthResponse switchCompany(Long userId, Long companyId);
    void validateUserBelongsToCompany(Long userId, Long companyId);
    CompanySettingsResponse getCompanySettings(Long companyId);
    CompanySettingsResponse updateCompanySettings(Long companyId, CompanySettingsRequest request);
    CompanyAttendanceLocationResponse getAttendanceLocation(Long companyId);
}
