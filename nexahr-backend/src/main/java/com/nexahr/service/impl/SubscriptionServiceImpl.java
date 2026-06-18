package com.nexahr.service.impl;

import com.nexahr.dto.response.SubscriptionResponse;
import com.nexahr.entity.Company;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.entity.enums.SubscriptionPlan;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.service.SubscriptionService;
import com.nexahr.util.PlanLimits;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public SubscriptionResponse getSubscription(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        SubscriptionPlan plan = resolvePlan(company.getPlan());
        int maxEmployees = PlanLimits.getMaxEmployees(plan);
        long currentEmployees = countEmployees(companyId);
        double usagePercent = maxEmployees == Integer.MAX_VALUE
                ? 0.0
                : Math.min(100.0, (currentEmployees * 100.0) / maxEmployees);

        List<String> features = new ArrayList<>(PlanLimits.getFeatures(plan).values());

        return SubscriptionResponse.builder()
                .plan(plan)
                .maxEmployees(maxEmployees == Integer.MAX_VALUE ? -1 : maxEmployees)
                .currentEmployees(currentEmployees)
                .usagePercent(usagePercent)
                .price(PlanLimits.getPrice(plan))
                .features(features)
                .billingEmail(company.getBillingEmail())
                .nextBillingDate(plan == SubscriptionPlan.FREE ? null : LocalDate.now().plusMonths(1).withDayOfMonth(1))
                .build();
    }

    @Override
    @Transactional
    public SubscriptionResponse upgradePlan(Long companyId, SubscriptionPlan plan) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        company.setPlan(plan.name());
        companyRepository.save(company);
        return getSubscription(companyId);
    }

    private SubscriptionPlan resolvePlan(String plan) {
        try {
            return SubscriptionPlan.valueOf(plan);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SubscriptionPlan.FREE;
        }
    }

    private long countEmployees(Long companyId) {
        return employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.ACTIVE, companyId)
                + employeeRepository.countByEmploymentStatusAndCompanyId(EmploymentStatus.PROBATION, companyId);
    }
}
