package com.nexahr.service.impl;

import com.nexahr.dto.response.OnboardingStatusResponse;
import com.nexahr.dto.response.OnboardingStepResponse;
import com.nexahr.entity.Company;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.DepartmentRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private static final int TOTAL_STEPS = 4;

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public OnboardingStatusResponse getStatus(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        return buildStatus(company);
    }

    @Override
    @Transactional
    public OnboardingStatusResponse updateStep(Long companyId, int step) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        company.setOnboardingStep(step);
        companyRepository.save(company);
        return buildStatus(company);
    }

    @Override
    @Transactional
    public OnboardingStatusResponse complete(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        company.setOnboardingCompleted(true);
        company.setOnboardingStep(TOTAL_STEPS);
        companyRepository.save(company);
        return buildStatus(company);
    }

    private OnboardingStatusResponse buildStatus(Company company) {
        boolean step1Done = StringUtils.hasText(company.getAddress());
        boolean step2Done = departmentRepository.countByCompanyId(company.getId()) > 0;
        boolean step3Done = employeeRepository.countByCompanyId(company.getId()) > 1;
        boolean step4Done = company.isOnboardingCompleted();

        List<OnboardingStepResponse> steps = List.of(
                OnboardingStepResponse.builder().key("company-info").title("Thông tin công ty").done(step1Done).build(),
                OnboardingStepResponse.builder().key("departments").title("Phòng ban").done(step2Done).build(),
                OnboardingStepResponse.builder().key("employees").title("Nhân viên").done(step3Done).build(),
                OnboardingStepResponse.builder().key("complete").title("Hoàn tất").done(step4Done).build()
        );

        return OnboardingStatusResponse.builder()
                .completed(company.isOnboardingCompleted())
                .currentStep(company.getOnboardingStep())
                .totalSteps(TOTAL_STEPS)
                .steps(steps)
                .build();
    }
}
