package com.nexahr.service.impl;

import com.nexahr.dto.request.CompanySettingsRequest;
import com.nexahr.dto.response.AuthResponse;
import com.nexahr.dto.response.CompanyAttendanceLocationResponse;
import com.nexahr.dto.response.CompanyResponse;
import com.nexahr.dto.response.CompanySettingsResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.CompanyMembership;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.CompanyStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyMembershipRepository;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.UserRepository;
import com.nexahr.service.AuthService;
import com.nexahr.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public List<CompanyResponse> getMyCompanies(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AuthResponse switchCompany(Long userId, Long companyId) {
        validateUserBelongsToCompany(userId, companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        if (company.getStatus() == CompanyStatus.SUSPENDED) {
            throw new BadRequestException("Công ty đã bị tạm ngưng");
        }

        membershipRepository.findByUserId(userId).forEach(m -> {
            m.setDefault(m.getCompany().getId().equals(companyId));
            membershipRepository.save(m);
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        return authService.issueAuthResponse(user, companyId);
    }

    @Override
    public void validateUserBelongsToCompany(Long userId, Long companyId) {
        if (!membershipRepository.existsByUserIdAndCompanyId(userId, companyId)) {
            throw new BadRequestException("Bạn không thuộc công ty này");
        }
    }

    @Override
    public CompanySettingsResponse getCompanySettings(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        return toSettingsResponse(company);
    }

    @Override
    @Transactional
    public CompanySettingsResponse updateCompanySettings(Long companyId, CompanySettingsRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        if (request.getName() != null) company.setName(request.getName());
        if (request.getLogo() != null) company.setLogo(request.getLogo());
        if (request.getAddress() != null) company.setAddress(request.getAddress());
        if (request.getPhone() != null) company.setPhone(request.getPhone());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getBillingEmail() != null) company.setBillingEmail(request.getBillingEmail());
        if (request.getCareersTagline() != null) company.setCareersTagline(request.getCareersTagline());
        if (request.getPrimaryColor() != null) company.setPrimaryColor(request.getPrimaryColor());
        if (request.getTimezone() != null) company.setTimezone(request.getTimezone());
        if (request.getLocale() != null) company.setLocale(request.getLocale());
        if (request.getDataRegion() != null) company.setDataRegion(request.getDataRegion());
        if (request.getLatitude() != null) company.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) company.setLongitude(request.getLongitude());
        if (request.getAttendanceRadiusMeters() != null) company.setAttendanceRadiusMeters(request.getAttendanceRadiusMeters());

        companyRepository.save(company);
        return toSettingsResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyAttendanceLocationResponse getAttendanceLocation(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        boolean configured = company.getLatitude() != null && company.getLongitude() != null;
        return CompanyAttendanceLocationResponse.builder()
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .radiusMeters(company.getAttendanceRadiusMeters() != null ? company.getAttendanceRadiusMeters() : 300)
                .address(company.getAddress())
                .configured(configured)
                .build();
    }

    private CompanySettingsResponse toSettingsResponse(Company company) {
        return CompanySettingsResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .code(company.getCode())
                .logo(company.getLogo())
                .address(company.getAddress())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .billingEmail(company.getBillingEmail())
                .primaryColor(company.getPrimaryColor())
                .careersTagline(company.getCareersTagline())
                .plan(company.getPlan())
                .timezone(company.getTimezone())
                .locale(company.getLocale())
                .dataRegion(company.getDataRegion())
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .attendanceRadiusMeters(company.getAttendanceRadiusMeters())
                .build();
    }

    private CompanyResponse toResponse(CompanyMembership membership) {
        Company company = membership.getCompany();
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .code(company.getCode())
                .logo(company.getLogo())
                .address(company.getAddress())
                .plan(company.getPlan())
                .status(company.getStatus())
                .onboardingCompleted(company.isOnboardingCompleted())
                .isDefault(membership.isDefault())
                .build();
    }
}
